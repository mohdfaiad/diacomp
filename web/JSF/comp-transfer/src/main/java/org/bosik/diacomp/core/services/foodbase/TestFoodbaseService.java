package org.bosik.diacomp.core.services.foodbase;

public interface TestFoodbaseService
{
	void test_addFindById_single_PersistedOk();

	void test_addFindAll_single_ReturnedNonEmpty();

	void test_addFindChanged_single_ReturnedNonEmpty();

	void test_delete_notFound_exceptionRaised();
}
