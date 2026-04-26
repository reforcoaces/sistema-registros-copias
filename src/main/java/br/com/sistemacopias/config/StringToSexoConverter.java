package br.com.sistemacopias.config;

import br.com.sistemacopias.model.Sexo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToSexoConverter implements Converter<String, Sexo> {

    @Override
    public Sexo convert(@NonNull String source) {
        if (source.isBlank()) {
            return null;
        }
        return Sexo.valueOf(source);
    }
}
