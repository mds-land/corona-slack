package com.github.mdsina.corona.view;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.model.ViewModelProcessor;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.inject.Singleton;

@Singleton
public class BaseUrlViewModelProcessor implements ViewModelProcessor {

    private final String baseUrl;

    public BaseUrlViewModelProcessor(@Value("${corona.base.url:}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void process(
        @Nonnull HttpRequest<?> request, @Nonnull ModelAndView<Map<String, Object>> modelAndView
    ) {
        Map<String, Object> viewModel = modelAndView.getModel().orElseGet(() -> {
            final HashMap<String, Object> newModel = new HashMap<>(1);
            modelAndView.setModel(newModel);
            return newModel;
        });
        viewModel.putIfAbsent("baseUrl", baseUrl);
    }
}
