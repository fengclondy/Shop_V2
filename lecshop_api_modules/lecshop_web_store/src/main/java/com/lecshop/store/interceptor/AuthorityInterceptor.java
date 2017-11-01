package com.lecshop.store.interceptor;

import com.lecshop.store.storeutil.StoreLoginUtils;
import com.lecshop.storemenu.bean.StoreMenu;
import com.lecshop.storemenu.service.StoreMenuService;
import com.lecshop.util.CommonConstant;
import com.lecshop.util.UnAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by dujinkai on 17/5/2.
 * 权限拦截器
 */
public class AuthorityInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private StoreMenuService storeMenuService;

    /**
     * 调试日志
     */
    private Logger logger = LoggerFactory.getLogger(AuthorityInterceptor.class);


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        sessionMenu(request);
        // 如果不需要拦截 则直接返回
        if (!isNeedFilter(handler)) {
            logger.debug("Donot need  authority");
            return true;
        }
        // 判断是否具有权限 没有选择 则直接返回没有权限提醒页面
        if (!hasAuth(request)) {
            logger.error("url is not access:{}", request.getServletPath());
            try {
                response.sendRedirect("noauth.jsp");
            } catch (IOException e) {
                logger.error("error:{}", e);
            }
            return false;
        }
        return true;
    }


    /**
     * 判断是否需要拦截
     */
    private boolean isNeedFilter(Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        return handlerMethod.getMethod().getAnnotation(UnAuth.class) == null;
    }

    /**
     * 判断用户是否有权限
     */
    private boolean hasAuth(HttpServletRequest request) {
        // 查询当前用户所有的权限
        List<StoreMenu> storeMenus = storeMenuService.queryAllStoreMenu(StoreLoginUtils.getInstance().getCustomerFromSession(request).getId());
        // 用户没有权限 直接返回false
        if (CollectionUtils.isEmpty(storeMenus)) {
            return false;
        }
        for (StoreMenu storeMenu : storeMenus) {
            if (StringUtils.isEmpty(storeMenu.getUrl())) {
                continue;
            }
            if (request.getServletPath().indexOf(storeMenu.getUrl()) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置Session里当前的菜单选项
     */
    public void sessionMenu(HttpServletRequest request) {
        if (request.getParameter(CommonConstant.STORE_MENU_FIRSTMENUS) != null) {
            request.getSession().setAttribute(CommonConstant.STORE_MENU_FIRSTMENUS, request.getParameter(CommonConstant.STORE_MENU_FIRSTMENUS));
        }
        if (request.getParameter(CommonConstant.STORE_MENU_SECONDMENUS) != null) {
            request.getSession().setAttribute(CommonConstant.STORE_MENU_SECONDMENUS, request.getParameter(CommonConstant.STORE_MENU_SECONDMENUS));
        }
        if (request.getParameter(CommonConstant.STORE_MENU_THIRDMENUS) != null) {
            request.getSession().setAttribute(CommonConstant.STORE_MENU_THIRDMENUS, request.getParameter(CommonConstant.STORE_MENU_THIRDMENUS));
        }
    }
}
