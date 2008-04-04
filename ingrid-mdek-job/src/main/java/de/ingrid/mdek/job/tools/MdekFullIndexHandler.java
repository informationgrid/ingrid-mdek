package de.ingrid.mdek.job.tools;

import java.util.Set;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.IFullIndexAccess;
import de.ingrid.mdek.services.persistence.db.model.AddressComment;
import de.ingrid.mdek.services.persistence.db.model.FullIndexAddr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T021Communication;
import de.ingrid.mdek.services.persistence.db.model.T02Address;


/**
 * Handles Update of Full Index.
 */
public class MdekFullIndexHandler implements IFullIndexAccess {

	protected MdekCatalogHandler catalogHandler;

	private IGenericDao<IEntity> daoFullIndexAddr;
	private ISysListDao daoSysList;

	private static MdekFullIndexHandler myInstance;

	/** Get The Singleton */
	public static synchronized MdekFullIndexHandler getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekFullIndexHandler(daoFactory);
	      }
		return myInstance;
	}

	private MdekFullIndexHandler(DaoFactory daoFactory) {
		catalogHandler = MdekCatalogHandler.getInstance(daoFactory);

		daoFullIndexAddr = daoFactory.getDao(FullIndexAddr.class);
		daoSysList = daoFactory.getSysListDao();
	}

	/** Updates data of given address in full index. */
	// TODO: pass AddressNode when associated with node instead of address
	public void updateAddressIndex(T02Address a) {
		// template for accessing data in index
		FullIndexAddr template = new FullIndexAddr();
		template.setAddrId(a.getId());

		// update full data
		template.setIdxName(IDX_NAME_FULLTEXT);
		FullIndexAddr idxEntry = (FullIndexAddr) daoFullIndexAddr.findUniqueByExample(template);		
		if (idxEntry == null) {
			idxEntry = template;
		}
		String data = getFullData(a);
		idxEntry.setIdxValue(data);
		
		daoFullIndexAddr.makePersistent(idxEntry);
	}

	/** Get full data of given address. */
	private String getFullData(T02Address a) {
		StringBuffer data = new StringBuffer();
		
		// Comments
		Set<AddressComment> comments = a.getAddressComments();
		for (AddressComment comment : comments) {
			extendFullData(data, comment.getComment());
		}
		// Search terms
		Set<SearchtermAdr> terms = a.getSearchtermAdrs();
		for (SearchtermAdr term : terms) {
			SearchtermValue termValue = term.getSearchtermValue();
			SearchtermType termType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termValue.getType());
			if (termType == SearchtermType.FREI) {
				extendFullData(data, termValue.getTerm());
			} else if (termType == SearchtermType.THESAURUS) {
				extendFullData(data, termValue.getSearchtermSns().getSnsId());
			}
		}
		// Communication values
		Set<T021Communication> comms = a.getT021Communications();
		for (T021Communication comm : comms) {
			extendFullData(data, comm.getCommValue());
		}
		// address data
		extendFullData(data, a.getAdrUuid());
		extendFullData(data, a.getOrgAdrId());
		extendFullData(data, a.getInstitution());
		extendFullData(data, a.getLastname());
		extendFullData(data, a.getFirstname());
		extendFullDataWithSysList(data, MdekSysList.ADDRESS, a.getAddressKey(), a.getAddressValue());
		extendFullDataWithSysList(data, MdekSysList.TITLE, a.getTitleKey(), a.getTitleValue());
		extendFullData(data, a.getStreet());
		extendFullData(data, a.getPostcode());
		extendFullData(data, a.getPostbox());
		extendFullData(data, a.getPostboxPc());
		extendFullData(data, a.getCity());
		extendFullData(data, a.getJob());
		extendFullData(data, a.getDescr());

		return data.toString();
	}

	/** Append a value to full data. also adds separator
	 * @param fullData full data where value is appended
	 * @param dataToAppend the value to append
	 */
	private void extendFullData(StringBuffer fullData, String dataToAppend) {
		fullData.append(IDX_SEPARATOR);
		fullData.append(dataToAppend);
	}

	/** Append SysList or free entry value to full data.
	 * @param fullData full data where value is appended
	 * @param sysList which syslist (see enumeration)
	 * @param sysListEntryId the entryId from bean
	 * @param freeValue the free value from bean
	 */
	private void extendFullDataWithSysList(StringBuffer fullData,
			MdekSysList sysList, int sysListEntryId, String freeValue) {
		if (MdekSysList.FREE_ENTRY.getDbValue().equals(sysListEntryId)) {
			extendFullData(fullData, freeValue);
		} else {
			String catalogLanguage = catalogHandler.getCatalogLanguage();
			SysList listEntry = daoSysList.getSysListEntry(sysList.getDbValue(), sysListEntryId, catalogLanguage);
			if (listEntry != null) {
				extendFullData(fullData, listEntry.getName());
			}
		}
	}
}
