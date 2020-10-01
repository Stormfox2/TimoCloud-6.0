package cloud.timo.TimoCloud.core.commands.defaults;

import cloud.timo.TimoCloud.common.encryption.RSAKeyUtil;
import cloud.timo.TimoCloud.core.TimoCloudCore;
import cloud.timo.TimoCloud.core.commands.Command;
import org.jline.builtins.Builtins;
import org.jline.reader.Completer;
import org.jline.reader.impl.completer.NullCompleter;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;

public class AddBaseCommand extends Command {

    public AddBaseCommand() {
        super("Addbase", "Addbase <Publickey>", "Adds a base to the core. Publickey is needed.");
    }

    @Override
    public boolean onCommand(Builtins.CommandInput input) {
        if (input.args().length < 1) {
            return false;
        }
        String publicKeyString = input.args()[0];
        try {
            PublicKey publicKey = RSAKeyUtil.publicKeyFromBase64(publicKeyString);
            TimoCloudCore.getInstance().getCorePublicKeyManager().addPermittedBaseKey(publicKey);
            TimoCloudCore.getInstance().getLogger().info("The public key has been permitted, so your base may connect now.");
        } catch (Exception e) {
            TimoCloudCore.getInstance().getLogger().severe(String.format("Invalid public key: '%s'. Please use the public key the base told you when you first started it. It is stored in the base/keys/public.tck.", publicKeyString));
        }
        return true;
    }

    @Override
    public List<Completer> complete(String command) {
        return Collections.singletonList(NullCompleter.INSTANCE);
    }
}
