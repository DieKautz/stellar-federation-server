package de.diekautz.federationserver.service

import de.diekautz.federationserver.datasource.FederationAddressDataSource
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class FederationServiceTest {

    private val dataSource: FederationAddressDataSource = mockk(relaxed = true)

    private val federationService = FederationService(dataSource)


    @Test
    fun `should call its datasource to retrieve fed account`() {
        //when
        federationService.getByFedAddress("")

        //then
        verify(exactly = 1) {
            dataSource.getAddresses()
        }
    }

}