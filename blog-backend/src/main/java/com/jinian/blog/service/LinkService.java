package com.jinian.blog.service;

import com.jinian.blog.dto.response.LinkResponse;
import com.jinian.blog.entity.Link;

import java.util.List;

/**
 * 友链服务接口
 */
public interface LinkService {

    List<LinkResponse> getAllLinks();

    LinkResponse getLinkById(Long id);

    LinkResponse createLink(Link link);

    LinkResponse updateLink(Long id, Link link);

    void deleteLink(Long id);
}