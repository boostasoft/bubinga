package com.boostasoft.goaway.tilingDB.datastructure.management;
/*
 * @author Dr Tsatcha D.
 */

import java.util.concurrent.ConcurrentHashMap;

import com.boostasoft.goaway.tilingDB.datastructure.hexaTree.HexaTreeIndex;

public class HexaTreeMultiLevel {

    
    ConcurrentHashMap<ScaleInfo,HexaTreeIndex> multiLevelIndex = new ConcurrentHashMap<ScaleInfo, HexaTreeIndex>();
    //ConcurrentHashMap<PointGeo,VoisinsNode> test1 = new ConcurrentHashMap<PointGeo, VoisinsNode>();

    public ConcurrentHashMap<ScaleInfo, HexaTreeIndex> getMultiLevelIndex() {
        return multiLevelIndex;
    }

    public void setMultiLevelIndex(
            ConcurrentHashMap<ScaleInfo, HexaTreeIndex> multiLevelIndex) {
        this.multiLevelIndex = multiLevelIndex;
    }

    public HexaTreeMultiLevel(
            ConcurrentHashMap<ScaleInfo, HexaTreeIndex> multiLevelIndexParam) {
        super();
        this.multiLevelIndex = multiLevelIndexParam;
    }
}
