package br.com.sistemacopias.support;

import br.com.sistemacopias.model.AppUser;
import br.com.sistemacopias.model.UserRole;

public final class ReforcoAccess {

    public static final String USERNAME_LUCILENE = "lucilene";

    private ReforcoAccess() {
    }

    public static boolean podeAcessarReforco(AppUser user) {
        if (user == null) {
            return false;
        }
        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }
        return USERNAME_LUCILENE.equalsIgnoreCase(user.getUsername().trim());
    }
}
