/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjGeo implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private String specialBase;
	private String method;
	private Double recExact;
	private Double recGrade;
	private Integer hierarchyLevel;

	private String transfParam;
    private Integer numDimensions;
    private String axisDimName;
    private Integer axisDimSize;
    private String cellGeometry;
    private String geoRectified;
    private String rectCheckpoint;
    private String rectDescription;
    private String rectCornerPoint;
    private String rectPointInPixel;
    private String refControlPoint;
    private String refOrientationParam;
    private String refGeoreferencedParam;
	
	private Double posAccuracyVertical;
	private Double gridPosAccuracy;
	private Integer keycInclWDataset;
	private String datasourceUuid;

	private Set t011ObjGeoDataBase = new HashSet();
	private Set t011ObjGeoAxisDim = new HashSet();
	private Set t011ObjGeoScales = new HashSet();
	private Set t011ObjGeoSpatialReps = new HashSet();
	private Set t011ObjGeoSupplinfos = new HashSet();
	private Set t011ObjGeoSymcs = new HashSet();
	private Set t011ObjGeoVectors = new HashSet();

	public T011ObjGeo() {}

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

	public Long getObjId() {
		return objId;
	}

	public void setObjId(Long objId) {
		this.objId = objId;
	}

	public String getSpecialBase() {
		return specialBase;
	}

	public void setSpecialBase(String specialBase) {
		this.specialBase = specialBase;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Double getRecExact() {
		return recExact;
	}

	public void setRecExact(Double recExact) {
		this.recExact = recExact;
	}

	public Double getRecGrade() {
		return recGrade;
	}

	public void setRecGrade(Double recGrade) {
		this.recGrade = recGrade;
	}

	public Integer getHierarchyLevel() {
		return hierarchyLevel;
	}

	public void setHierarchyLevel(Integer hierarchyLevel) {
		this.hierarchyLevel = hierarchyLevel;
	}

	public Double getPosAccuracyVertical() {
		return posAccuracyVertical;
	}

	public void setPosAccuracyVertical(Double posAccuracyVertical) {
		this.posAccuracyVertical = posAccuracyVertical;
	}

	public Integer getKeycInclWDataset() {
		return keycInclWDataset;
	}

	public void setKeycInclWDataset(Integer keycInclWDataset) {
		this.keycInclWDataset = keycInclWDataset;
	}

	public String getDatasourceUuid() {
		return datasourceUuid;
	}

	public void setDatasourceUuid(String datasourceUuid) {
		this.datasourceUuid = datasourceUuid;
	}

	public Set getT011ObjGeoDataBase() {
		return t011ObjGeoDataBase;
	}

	public void setT011ObjGeoDataBase(Set t011ObjGeoDataBase) {
		this.t011ObjGeoDataBase = t011ObjGeoDataBase;
	}


	public Set getT011ObjGeoAxisDim() {
		return t011ObjGeoAxisDim;
	}

	public void setT011ObjGeoAxisDim(Set t011ObjGeoAxisDim) {
		this.t011ObjGeoAxisDim = t011ObjGeoAxisDim;
	}

	public Set getT011ObjGeoScales() {
		return t011ObjGeoScales;
	}

	public void setT011ObjGeoScales(Set t011ObjGeoScales) {
		this.t011ObjGeoScales = t011ObjGeoScales;
	}

	public Set getT011ObjGeoSpatialReps() {
		return t011ObjGeoSpatialReps;
	}

	public void setT011ObjGeoSpatialReps(Set t011ObjGeoSpatialReps) {
		this.t011ObjGeoSpatialReps = t011ObjGeoSpatialReps;
	}

	public Set getT011ObjGeoSupplinfos() {
		return t011ObjGeoSupplinfos;
	}

	public void setT011ObjGeoSupplinfos(Set t011ObjGeoSupplinfos) {
		this.t011ObjGeoSupplinfos = t011ObjGeoSupplinfos;
	}

	public Set getT011ObjGeoSymcs() {
		return t011ObjGeoSymcs;
	}

	public void setT011ObjGeoSymcs(Set t011ObjGeoSymcs) {
		this.t011ObjGeoSymcs = t011ObjGeoSymcs;
	}

	public Set getT011ObjGeoVectors() {
		return t011ObjGeoVectors;
	}

	public void setT011ObjGeoVectors(Set t011ObjGeoVectors) {
		this.t011ObjGeoVectors = t011ObjGeoVectors;
	}

    public Double getGridPosAccuracy() {
        return gridPosAccuracy;
    }

    public void setGridPosAccuracy(Double gridPosAccuracy) {
        this.gridPosAccuracy = gridPosAccuracy;
    }

    public String getTransfParam() {
        return transfParam;
    }

    public void setTransfParam(String transfParam) {
        this.transfParam = transfParam;
    }

    public Integer getNumDimensions() {
        return numDimensions;
    }

    public void setNumDimensions(Integer numDimensions) {
        this.numDimensions = numDimensions;
    }

    public String getAxisDimName() {
        return axisDimName;
    }

    public void setAxisDimName(String axisDimName) {
        this.axisDimName = axisDimName;
    }

    public Integer getAxisDimSize() {
        return axisDimSize;
    }

    public void setAxisDimSize(Integer axisDimSize) {
        this.axisDimSize = axisDimSize;
    }

    public String getCellGeometry() {
        return cellGeometry;
    }

    public void setCellGeometry(String cellGeometry) {
        this.cellGeometry = cellGeometry;
    }

    public String getGeoRectified() {
        return geoRectified;
    }

    public void setGeoRectified(String geoRectified) {
        this.geoRectified = geoRectified;
    }

    public String getRectCheckpoint() {
        return rectCheckpoint;
    }

    public void setRectCheckpoint(String rectCheckpoint) {
        this.rectCheckpoint = rectCheckpoint;
    }

    public String getRectDescription() {
        return rectDescription;
    }

    public void setRectDescription(String rectDescription) {
        this.rectDescription = rectDescription;
    }

    public String getRectCornerPoint() {
        return rectCornerPoint;
    }

    public void setRectCornerPoint(String rectCornerPoint) {
        this.rectCornerPoint = rectCornerPoint;
    }

    public String getRectPointInPixel() {
        return rectPointInPixel;
    }

    public void setRectPointInPixel(String rectPointInPixel) {
        this.rectPointInPixel = rectPointInPixel;
    }

    public String getRefOrientationParam() {
        return refOrientationParam;
    }

    public void setRefOrientationParam(String refOrientationParam) {
        this.refOrientationParam = refOrientationParam;
    }

    public String getRefGeoreferencedParam() {
        return refGeoreferencedParam;
    }

    public void setRefGeoreferencedParam(String refGeoreferencedParam) {
        this.refGeoreferencedParam = refGeoreferencedParam;
    }

    public String getRefControlPoint() {
        return refControlPoint;
    }

    public void setRefControlPoint(String refControlPoint) {
        this.refControlPoint = refControlPoint;
    }

}
