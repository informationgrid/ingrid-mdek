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
