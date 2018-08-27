/*
 * Title: TopicModel.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

/*
 * Title: TopicModel.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

/*
 * Title: TopicModel.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicmodeling.models;

import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.results.TMResults;

public interface TopicModel {

    /**
     * interface for topic modeling classes
     */
    TMResults run(TDialog dialog);

}
