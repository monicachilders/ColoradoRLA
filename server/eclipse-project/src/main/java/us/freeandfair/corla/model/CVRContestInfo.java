/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Aug 2, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Daniel M. Zimmerman <dmz@galois.com>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.model;

import static us.freeandfair.corla.util.EqualsHashcodeHelper.nullableEquals;
import static us.freeandfair.corla.util.EqualsHashcodeHelper.nullableHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import com.google.gson.annotations.JsonAdapter;

import us.freeandfair.corla.gson.CVRContestInfoJsonAdapter;
import us.freeandfair.corla.hibernate.AbstractEntity;

/**
 * A cast vote record contains information about a single ballot, either 
 * imported from a tabulator export file or generated by auditors.
 * 
 * @author Daniel M. Zimmerman
 * @version 0.0.1
 */
@Entity
@Table(name = "cvr_contest_info")
//this class has many fields that would normally be declared final, but
//cannot be for compatibility with Hibernate and JPA.
@SuppressWarnings("PMD.ImmutableField")
@JsonAdapter(CVRContestInfoJsonAdapter.class)
public class CVRContestInfo extends AbstractEntity implements Serializable {
  /**
   * The serialVersionUID.
   */
  private static final long serialVersionUID = 1L;
  
  /**
   * The CVR to which this record belongs. 
   */
  @ManyToOne(optional = false)
  @JoinColumn
  private CastVoteRecord my_cvr;
  
  /**
   * The contest in this record.
   */
  @ManyToOne(optional = false)
  private Contest my_contest;
  
  /** 
   * The comment for this contest.
   */
  @Column(updatable = false)
  private String my_comment;
  
  /**
   * The consensus value for this contest
   */
  @Column(updatable = false)
  @Enumerated(EnumType.STRING)
  private ConsensusValue my_consensus;
  
  /**
   * The choices for this contest.
   */
  // this is a list of choice names to make persistence more straightforward; if it
  // were a list of Choice, then the mapping between contests and choices would
  // need to be more complex
  @ElementCollection(fetch = FetchType.EAGER)
  @OrderColumn(name = "index")
  private List<String> my_choices;

  /**
   * Constructs an empty CVRContestInfo, solely for persistence.
   */
  public CVRContestInfo() {
    super();
  }
  
  /**
   * Constructs a CVR contest information record with the specified 
   * parameters.
   * 
   * @param the_contest The contest.
   * @param the_comment The comment.
   * @param the_consensus The consensus value.
   * @param the_choices The choices.
   * @exception IllegalArgumentException if any choice is not a valid choice
   * for the specified contest.
   */
  public CVRContestInfo(final Contest the_contest, final String the_comment,
                        final ConsensusValue the_consensus,
                        final List<String> the_choices) {
    super();
    my_contest = the_contest;
    my_comment = the_comment;
    my_consensus = the_consensus;
    my_choices = new ArrayList<String>(the_choices);
    for (final String s : my_choices) {
      if (!my_contest.isValidChoice(s)) {
        throw new IllegalArgumentException("invalid choice " + s + 
                                           " for contest " + my_contest);
      }
    }
  }
  
  /**
   * Sets the CVR that owns this record; this should only be called by
   * the CastVoteRecord class.
   * 
   * @param the_cvr The CVR.
   */
  protected void setCVR(final CastVoteRecord the_cvr) {
    my_cvr = the_cvr;
  }

  /**
   * @return the CVR that owns this record.
   */
  public CastVoteRecord cvr() {
    return my_cvr;
  }
  
  /**
   * @return the contest in this record.
   */
  public Contest contest() {
    return my_contest;
  }
  
  /**
   * @return the comment in this record.
   */
  public String comment() {
    return my_comment;
  }
  
  /**
   * @return the consensus flag in this record.
   */
  public ConsensusValue consensus() {
    return my_consensus;
  }
  
  /**
   * @return the choices in this record.
   */
  public List<String> choices() {
    return Collections.unmodifiableList(my_choices);
  }
  
  /**
   * @return a String representation of this cast vote record.
   */
  @Override
  public String toString() {
    return "CVRContestInfo [contest=" + my_contest + ", comment=" + 
           my_comment + ", consensus=" + my_consensus + ", choices=" +
           my_choices + "]";
  }
  
  /**
   * Compare this object with another for equivalence.
   * 
   * @param the_other The other object.
   * @return true if the objects are equivalent, false otherwise.
   */
  @Override
  public boolean equals(final Object the_other) {
    boolean result = true;
    if (the_other instanceof CVRContestInfo) {
      final CVRContestInfo other_info = (CVRContestInfo) the_other;
      result &= nullableEquals(other_info.contest(), contest());
      result &= nullableEquals(other_info.comment(), comment());
      result &= nullableEquals(other_info.consensus(), consensus());
      result &= nullableEquals(other_info.choices(), choices());
    } else {
      result = false;
    }
    return result;
  }
  
  /**
   * @return a hash code for this object.
   */
  @Override
  public int hashCode() {
    // can't just use toString() because order of choices may differ
    return (contest() + comment() + nullableHashCode(consensus()) + 
            nullableHashCode(choices())).hashCode();
  }

  /**
   * The possible values for consensus.
   */
  public enum ConsensusValue {
    YES,
    NO
  }
}
