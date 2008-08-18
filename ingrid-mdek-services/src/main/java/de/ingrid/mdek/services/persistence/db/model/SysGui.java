package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class SysGui implements IEntity {

	private Long id;
	private int version;
	private String guiId;
	private Integer behaviour;


	public SysGui() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getGuiId() {
		return guiId;
	}

	public void setGuiId(String guiId) {
		this.guiId = guiId;
	}

	public Integer getBehaviour() {
		return behaviour;
	}

	public void setBehaviour(Integer behaviour) {
		this.behaviour = behaviour;
	}


}