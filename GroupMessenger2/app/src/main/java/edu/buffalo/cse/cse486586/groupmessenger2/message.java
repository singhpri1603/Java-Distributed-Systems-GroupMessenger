package edu.buffalo.cse.cse486586.groupmessenger2;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by priyanka on 05/03/16.
 */
public class message implements Serializable{

    int tag;
    int msgeID;
    String msge;
    int proposal;
    String orig_process_id;
    String ppsl_process_id;

public boolean equals(Object o)
{if(o instanceof  message)
{
    message temp=(message)o;
    return msgeID==temp.msgeID && orig_process_id.equals(temp.orig_process_id);
}
    return false;

}

}


