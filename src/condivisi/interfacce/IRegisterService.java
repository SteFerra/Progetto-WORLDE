package condivisi.interfacce;

import java.rmi.Remote;

// interfaccia per invocare il comando REGISTER sul server attraverso RMI
public interface IRegisterService extends Remote {
    int register(String username, String password) throws Exception;
}
