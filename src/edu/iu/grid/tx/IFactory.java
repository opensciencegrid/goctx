package edu.iu.grid.tx;

import edu.iu.grid.tx.accessor.TicketAccessor;
import edu.iu.grid.tx.converter.TicketConverter;

public interface IFactory {
	TicketConverter chooseAndInstantiateConverter(TicketAccessor get, TicketAccessor set) throws Exception;
	TicketExchanger createInstance(String tx_key) throws Exception;
}
