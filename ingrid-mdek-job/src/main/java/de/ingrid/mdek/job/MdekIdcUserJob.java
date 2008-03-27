package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekKeysUser;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IIdcGroupDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapperUser;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning USER MANAGEMENT (permissions etc.). 
 */
public class MdekIdcUserJob extends MdekIdcJob {

	/** service encapsulating user management functionality */
	IPermissionService userHandler;

	private IIdcGroupDao daoIdcGroup;

	protected BeanToDocMapperUser beanToDocMapperUser;

	public MdekIdcUserJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionHandler) {
		super(logService.getLogger(MdekIdcUserJob.class), daoFactory);
		
		this.userHandler = permissionHandler;
		
		daoIdcGroup = daoFactory.getIdcGroupDao();

		beanToDocMapperUser = BeanToDocMapperUser.getInstance();
	}

	public IngridDocument getGroups(IngridDocument params) {
		try {
			daoIdcGroup.beginTransaction();

			List<IdcGroup> groups = daoIdcGroup.getGroups();

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(groups.size());
			for (IdcGroup group : groups) {
				IngridDocument groupDoc = new IngridDocument();
				beanToDocMapperUser.mapIdcGroup(group, groupDoc, MappingQuantity.BASIC_ENTITY);
				resultList.add(groupDoc);
			}

			IngridDocument result = new IngridDocument();
			result.put(MdekKeysUser.GROUPS, resultList);

			daoIdcGroup.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoIdcGroup.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}
}
