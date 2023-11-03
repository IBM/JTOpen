/**
 * Provides classes that efficiently access lists of resources on an IBM i
 * system.
 *
 * <P>
 * Classes in this package are wrappers around the IBM i "Open List" (QGY)
 * system APIs. A list of resources is compiled on the system to satisfy certain
 * filter criteria. The list can then be accessed sequentially or randomly by
 * the client.
 * </P>
 *
 * <P>
 * Users can implement their own Open List API wrappers by extending the
 * com.ibm.as400.access.list.OpenList class.
 * </P>
 */
package com.ibm.as400.access.list;
