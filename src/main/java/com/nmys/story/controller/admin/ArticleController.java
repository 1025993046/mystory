package com.nmys.story.controller.admin;

import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.page.Page;
import com.blade.kit.StringKit;
import com.blade.mvc.annotation.*;
import com.blade.mvc.http.Request;
import com.blade.mvc.ui.RestResponse;
import com.blade.validator.annotation.Valid;
import com.nmys.story.controller.BaseController;
import com.nmys.story.exception.TipException;
import com.nmys.story.extension.Commons;
import com.nmys.story.model.dto.LogActions;
import com.nmys.story.model.dto.Types;
import com.nmys.story.model.entity.Contents;
import com.nmys.story.model.entity.Logs;
import com.nmys.story.model.entity.Metas;
import com.nmys.story.model.entity.Users;
import com.nmys.story.service.ContentsService;
import com.nmys.story.service.MetasService;
import com.nmys.story.service.SiteService;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

/**
 * 文章管理控制器
 * Created by biezhi on 2017/2/21.
 */
@Slf4j
@Path("admin/article")
public class ArticleController extends BaseController {

    @Inject
    private ContentsService contentsService;

    @Inject
    private MetasService metasService;

    @Inject
    private SiteService siteService;

    /**
     * 文章管理首页
     *
     * @param page
     * @param limit
     * @param request
     * @return
     */
    @GetRoute(value = "")
    public String index(@Param(defaultValue = "1") int page, @Param(defaultValue = "15") int limit,
                        Request request) {

        Page<Contents> articles = new Contents().where("type", Types.ARTICLE).page(page, limit, "created desc");
        request.attribute("articles", articles);
        return "admin/article_list";
    }

    /**
     * 文章发布页面
     *
     * @param request
     * @return
     */
    @GetRoute(value = "publish")
    public String newArticle(Request request) {
        List<Metas> categories = metasService.getMetas(Types.CATEGORY);
        request.attribute("categories", categories);
        request.attribute(Types.ATTACH_URL, Commons.site_option(Types.ATTACH_URL, Commons.site_url()));
        return "admin/article_edit";
    }

    /**
     * 文章编辑页面
     *
     * @param cid
     * @param request
     * @return
     */
    @GetRoute(value = "/:cid")
    public String editArticle(@PathParam String cid, Request request) {
        Optional<Contents> contents = contentsService.getContents(cid);
        if (!contents.isPresent()) {
            return render_404();
        }
        request.attribute("contents", contents.get());
        List<Metas> categories = metasService.getMetas(Types.CATEGORY);
        request.attribute("categories", categories);
        request.attribute("active", "article");
        request.attribute(Types.ATTACH_URL, Commons.site_option(Types.ATTACH_URL, Commons.site_url()));
        return "admin/article_edit";
    }

    /**
     * 发布文章操作
     *
     * @return
     */
    @PostRoute(value = "publish")
    @JSON
    public RestResponse publishArticle(@Valid Contents contents) {
//        Users users = this.user();
//        contents.setType(Types.ARTICLE);
//        contents.setAuthorId(users.getUid());
//        if (StringKit.isBlank(contents.getCategories())) {
//            contents.setCategories("默认分类");
//        }
//
//        try {
//            Integer cid = contentsService.publish(contents);
//            siteService.cleanCache(Types.C_STATISTICS);
//            return RestResponse.ok(cid);
//        } catch (Exception e) {
//            String msg = "文章发布失败";
//            if (e instanceof TipException) {
//                msg = e.getMessage();
//            } else {
//                log.error(msg, e);
//            }
//            return RestResponse.fail(msg);
//        }
        return null;
    }

    /**
     * 修改文章操作
     *
     * @return
     */
    @PostRoute(value = "modify")
    @JSON
    public RestResponse modifyArticle(@Valid Contents contents) {
        try {
            if (null == contents || null == contents.getCid()) {
                return RestResponse.fail("缺少参数，请重试");
            }
            Integer cid = contents.getCid();
            contentsService.updateArticle(contents);
            return RestResponse.ok(cid);
        } catch (Exception e) {
            String msg = "文章编辑失败";
            if (e instanceof TipException) {
                msg = e.getMessage();
            } else {
                log.error(msg, e);
            }
            return RestResponse.fail(msg);
        }
    }

    /**
     * 删除文章操作
     *
     * @param cid
     * @param request
     * @return
     */
    @Route(value = "delete")
    @JSON
    public RestResponse delete(@Param int cid, HttpServletRequest request) {
        try {
            contentsService.delete(cid);
            siteService.cleanCache(Types.C_STATISTICS);
            new Logs(LogActions.DEL_ARTICLE, cid + "", request.getRemoteAddr(), this.getUid(request)).save();
        } catch (Exception e) {
            String msg = "文章删除失败";
            if (e instanceof TipException) {
                msg = e.getMessage();
            } else {
                log.error(msg, e);
            }
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }
}
