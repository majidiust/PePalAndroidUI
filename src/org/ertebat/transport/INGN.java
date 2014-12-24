/**
 * 
 */
package org.ertebat.transport;

import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;

/**
 * @author Majid
 *
 */
public interface INGN {
	public void SetCallState(NgnInviteEventTypes callState);
	public void OnIncommingCall(NgnInviteEventArgs args);
}
