/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
package de.ingrid.mdek.job;

import de.ingrid.mdek.job.IJob;
import de.ingrid.utils.IngridDocument;

public class DummyJob implements IJob {

	private int _y;

	private int _x;

	public DummyJob() {
	}

	public IngridDocument getResults() {
		return new IngridDocument();
	}

	public boolean isRunning() {
		return false;
	}

	public boolean start() {
		return false;
	}

	public boolean stop() {
		return false;
	}

	public int sum() {
		return _x + _y;
	}

	public void setX(int x) {
		_x = x;

	}

	public void setY(int y) {
		_y = y;
	}

	public int getX() {
		return _x;
	}

	public int getY() {
		return _y;
	}

}
