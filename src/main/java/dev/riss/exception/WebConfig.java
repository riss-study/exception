package dev.riss.exception;

import dev.riss.exception.filter.LogFilter;
import dev.riss.exception.interceptor.LogInterceptor;
import dev.riss.exception.resolver.MyHandlerExceptionResolver;
import dev.riss.exception.resolver.UserHandlerExceptionResolver;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

//    private final LogFilter logFilter;
    private final LogInterceptor logInterceptor;
    private final MyHandlerExceptionResolver myHandlerExceptionResolver;
    private final UserHandlerExceptionResolver userHandlerExceptionResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .order(Integer.MIN_VALUE)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "*.ico", "/error", "/error-page/**");
        // 인터셉터는 dispatcherType 을 설정하지 못하고, 대신 오류 페이지 경로들을 excludePathPatterns() 를 통해 배제할 수 있음 (화이트리스트)
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(myHandlerExceptionResolver);
        resolvers.add(userHandlerExceptionResolver);
    }

    //    @Bean
//    public FilterRegistrationBean logFilterRegistration () {
//        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
//        filterRegistrationBean.setFilter(logFilter);
//        filterRegistrationBean.setOrder(Integer.MIN_VALUE);
//        filterRegistrationBean.addUrlPatterns("/*");
//        // dispatcherType 이 REQUEST, ERROR 인 경우 호출되도록 설정
//        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST); //, DispatcherType.ERROR);
//        return filterRegistrationBean;
//    }


}
