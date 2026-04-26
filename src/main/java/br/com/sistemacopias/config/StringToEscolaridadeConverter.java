package br.com.sistemacopias.config;

import br.com.sistemacopias.model.Escolaridade;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Permite no formulario um &lt;option value=""&gt; para &quot;Selecione a serie&quot; antes do enum.
 */
@Component
public class StringToEscolaridadeConverter implements Converter<String, Escolaridade> {

    @Override
    public Escolaridade convert(@NonNull String source) {
        if (source.isBlank()) {
            return null;
        }
        return Escolaridade.valueOf(source);
    }
}
