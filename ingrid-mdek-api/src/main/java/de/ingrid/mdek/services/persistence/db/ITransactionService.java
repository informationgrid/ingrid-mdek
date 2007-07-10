package de.ingrid.mdek.services.persistence.db;

public interface ITransactionService {

	void beginTransaction();

	void commitTransaction();

	void rollbackTransaction();
}
