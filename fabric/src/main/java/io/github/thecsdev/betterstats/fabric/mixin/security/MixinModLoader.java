package io.github.thecsdev.betterstats.fabric.mixin.security;

import io.github.thecsdev.betterstats.fabric.BetterStatsFabric;
import io.github.thecsdev.tcdcommons.api.util.integrity.SelfDefense;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BetterStatsFabric.class, priority = 9001)
public abstract class MixinModLoader
{
	@Inject(method = "<clinit>", at = @At("HEAD"), cancellable = true, require = 0)
	private static void onClassInit(CallbackInfo callback)
	{
		SelfDefense.reportClassInitializer(BetterStatsFabric.class);
		callback.cancel();
		
		/*//construct the message
		final var fullName = BetterStatsFabric.class.getName();
		final var message = String.format(
				"AN INTEGRITY VIOLATION WAS FOUND:\n"
				+ "The class '%s' has a static constructor, which isn't allowed!\n"
				+ "The game will now close. Please run a virus scan in the meantime.", fullName);
		
		//terminate the program
		throw new ExceptionInInitializerError(message);*/
		
		/* ^ IMPORTANT NOTE: if you have a static constructor defined, or a static field defined,
		 * the above code WILL end up always executing, even when it isn't supposed to, which is bad.
		 * 
		 * if you absolutely have to have a static field, then use the approach below instead
		 * (just make sure to not initialize it with the `=` sign or in a static constructor):
		 */
		
		//not allowed to execute code in static constructor/initializer
		//callback.cancel(); -- only use if the above approach is infeasible
	}
}