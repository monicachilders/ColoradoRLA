/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Aug 8, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Joe Kiniry <kiniry@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.asm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import us.freeandfair.corla.asm.AsmEvent.CountyDashboardEvent;
import us.freeandfair.corla.asm.AsmState.CountyDashboardState;
import us.freeandfair.corla.asm.AsmTransitions.CountyDashboardTransitions;
import us.freeandfair.corla.util.Pair;

/**
 * The ASM for the County Dashboard.
 * @trace asm.dos_dashboard_next_state
 */
public class CountyDashboardAsm extends AbstractAsm {
  /**
   * Create the County Dashboard ASM.
   * @trace asm.county_dashboard_asm
   */
  public CountyDashboardAsm() {
    super();
    final Set<AsmState> states = new HashSet<AsmState>();
    for (final AsmState s : CountyDashboardState.values()) {
      states.add(s);
    }
    final Set<AsmEvent> events = new HashSet<AsmEvent>();
    for (final AsmEvent e : CountyDashboardEvent.values()) {
      events.add(e);
    }
    final Map<Pair<AsmState, AsmEvent>, AsmState> map = 
        new HashMap<Pair<AsmState, AsmEvent>, AsmState>();
    for (final CountyDashboardTransitions t : 
        CountyDashboardTransitions.values()) {
      map.put(t.my_pair.getFirst(), t.my_pair.getSecond());
    }
    final Set<AsmState> final_states = new HashSet<AsmState>();
    final_states.add(CountyDashboardState.UPLOAD_VERIFIED_CVRS_UPLOAD_SUCESSFUL);
    initialize(states, events, map, 
               CountyDashboardState.INITIAL_STATE,
               final_states);
  } 
}
