package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.Comparator;

/**
 * Created by priyanka on 06/03/16.
 */
public class QueueComparator implements Comparator<message> {
    @Override
    public int compare(message lhs, message rhs) {

        if (lhs.proposal > rhs.proposal) {
            return 1;
        } else if (lhs.proposal < rhs.proposal) {
            return -1;
        } else {
            if (Integer.parseInt(lhs.ppsl_process_id) > Integer.parseInt(rhs.ppsl_process_id)) {
                return 1;
            } else if (Integer.parseInt(lhs.ppsl_process_id) < Integer.parseInt(rhs.ppsl_process_id)) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}