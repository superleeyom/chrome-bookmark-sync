package com.bookmark.action;

import com.bookmark.action.util.GenerateReadmeUtil;

/**
 * 启动类
 *
 * @author leeyom
 */
public class ChromeBookmarksSyncApplication {

    public static void main(String[] args) {
        GenerateReadmeUtil.generateReadme();
        System.exit(0);
    }

}
