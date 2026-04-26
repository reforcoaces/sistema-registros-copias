package br.com.sistemacopias.config;

import br.com.sistemacopias.model.RecorrenciaMensalidade;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToRecorrenciaMensalidadeConverter implements Converter<String, RecorrenciaMensalidade> {

    @Override
    public RecorrenciaMensalidade convert(@NonNull String source) {
        if (source.isBlank()) {
            return null;
        }
        return RecorrenciaMensalidade.valueOf(source);
    }
}
