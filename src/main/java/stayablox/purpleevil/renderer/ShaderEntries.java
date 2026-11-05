package stayablox.purpleevil.renderer;

import com.ferra13671.cometrenderer.glsl.GLSLLoader;
import com.ferra13671.cometrenderer.glsl.compiler.GLSLFileEntry;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ShaderEntries {

    public final GLSLFileEntry DEFAULT_VERTEX = GLSLFileEntry.load("default_vertex", GLSLLoader.IN_JAR, "assets/shaders/default.vsh");
    public final GLSLFileEntry DEFAULT_FRAGMENT = GLSLFileEntry.load("default_fragment", GLSLLoader.IN_JAR, "assets/shaders/default.fsh");
    public final GLSLFileEntry FOG_VERTEX = GLSLFileEntry.load("fog_vertex", GLSLLoader.IN_JAR, "assets/shaders/fog.vsh");
    public final GLSLFileEntry FOG_FRAGMENT = GLSLFileEntry.load("fog_fragment", GLSLLoader.IN_JAR, "assets/shaders/fog.fsh");
}
