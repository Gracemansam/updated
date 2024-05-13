//package com.kenpb.app.security;
//
//
//import com.kenpb.app.security.annotation.RequirePermission;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.springframework.security.authentication.InsufficientAuthenticationException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.util.StringUtils;
//
//public class PermissionCheckingAspect {
//
//    @Around("@annotation(permission)")
//    public Object check(ProceedingJoinPoint joinPoint, RequirePermission permission) throws Throwable {
//        String requiredRole = "ROLE_"+permission.value();
//        if (!StringUtils.isEmpty(requiredRole)) {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            UserDetails user = null;
//            if (authentication != null && authentication.isAuthenticated()) {
//                user = (UserDetails) authentication.getPrincipal();
//            }
//            if (user == null
//                    || user.getAuthorities() == null
//                    || user.getAuthorities().stream().noneMatch(auth -> requiredRole.equalsIgnoreCase(auth.getAuthority()))) {
//                throw new InsufficientAuthenticationException("Permission Denied");
//            }
//        }
//        return joinPoint.proceed();
//    }
//}
