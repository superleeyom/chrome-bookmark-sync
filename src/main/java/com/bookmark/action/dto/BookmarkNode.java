package com.bookmark.action.dto;

import lombok.Data;

import java.util.List;

/**
 * 书签节点
 *
 * @author leeyom wang
 * @date 2020/12/4 上午11:50
 */
@Data
public class BookmarkNode {

    /**
     * 主键
     */
    private String id;

    /**
     * 索引
     */
    private String index;

    /**
     * 父级id
     */
    private String parentId;

    /**
     * 添加时间戳
     */
    private long dateAdded;

    /**
     * 文件夹修改时间
     */
    private long dateGroupModified;

    /**
     * 标题
     */
    private String title;

    /**
     * url
     */
    private String url;

    /**
     * 子节点
     */
    private List<BookmarkNode> children;

}
