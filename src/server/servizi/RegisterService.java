package server.servizi;

import condivisi.interfacce.IRegisterService;
import server.admin.UserAdmin;

import java.util.List;

// classe che implementa l'interfaccia RMI per il comando "REGISTER"
public class RegisterService implements IRegisterService {

    private final UserAdmin useradmin;

    public RegisterService(UserAdmin useradmin){
        this.useradmin= useradmin;
    }

    // effettua registrazione utilizzando l'istanza di UserAdmin
    @Override
    public int register(String username, String password) throws Exception {
        return useradmin.register(username,password);
    }
}
