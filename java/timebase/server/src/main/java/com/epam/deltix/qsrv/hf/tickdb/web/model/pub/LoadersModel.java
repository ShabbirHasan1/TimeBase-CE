package com.epam.deltix.qsrv.hf.tickdb.web.model.pub;

import com.epam.deltix.qsrv.hf.tickdb.pub.mon.TBLoader;

/**
 *
 */
public interface LoadersModel extends TimeBaseModel {

    TBLoader[]          getOpenLoaders();

}
