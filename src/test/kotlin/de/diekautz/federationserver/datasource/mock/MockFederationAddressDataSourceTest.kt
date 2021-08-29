package de.diekautz.federationserver.datasource.mock

import de.diekautz.federationserver.model.MemoType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.junit.jupiter.api.Test

internal class MockFederationAddressDataSourceTest {

    private val mockDataSource = MockFederationAddressDataSource()
    
    @Test
    fun `should provide list of FedAddresses`() {
        //when
        val accs = mockDataSource.getAddresses()
        
        //then
        assertThat(accs).isNotEmpty
    }

    @Test
    fun `should provide mock data`() {
        //when
        val accs = mockDataSource.getAddresses()

        //then
        assertThat(accs)
            .allSatisfy { it ->
                assertThat(it.accountID).isNotEmpty
                assertThat(it.stellarAddress).isNotEmpty
            }
    }

    @Test
    fun `should memo type and content should match`() {
        //when
        val accs = mockDataSource.getAddresses()

        //then
        assertThat(accs).allSatisfy {
            assertThat(it).satisfies(Condition ({ fedAcc ->
                if (fedAcc.memoType == MemoType.HASH && fedAcc.memo != null) {
                    return@Condition fedAcc.memo!!.matches(Regex("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?\$"))
                }
                (fedAcc.memoType == MemoType.NONE) == (fedAcc.memo == null)
            }, "Memo Type matches given memo"))
        }
    }
    
}