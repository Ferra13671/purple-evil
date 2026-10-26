package stayablox.purpleevil.renderer;

import com.ferra13671.cometrenderer.glsl.GLSLLoader;
import com.ferra13671.cometrenderer.glsl.compiler.GLSLFileEntry;
import com.ferra13671.cometrenderer.glsl.uniform.UniformType;
import com.ferra13671.cometrenderer.plugins.bettercompiler.BetterCompilerPlugin;
import com.ferra13671.cometrenderer.plugins.bettercompiler.GLShaderLibraryBuilder;

public class ShaderLibraries {

    public final GLSLFileEntry matrices = new GLShaderLibraryBuilder<>(GLSLLoader.IN_JAR)
            .name("matrices")
            .library("assets/shaders/libraries/matrices.glsl")
            .uniform("projection", UniformType.MATRIX4)
            .uniform("view", UniformType.MATRIX4)
            .build();

    public ShaderLibraries() {
        BetterCompilerPlugin.registerShaderLibraries(
                this.matrices
        );
    }
}
