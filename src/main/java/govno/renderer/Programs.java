package govno.renderer;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.glsl.GLProgram;
import com.ferra13671.cometrenderer.glsl.GLSLLoader;
import com.ferra13671.cometrenderer.glsl.shader.GLShader;
import com.ferra13671.cometrenderer.glsl.shader.ShaderType;
import com.ferra13671.cometrenderer.plugins.bettercompiler.BetterCompilerBuilderInfo;
import com.ferra13671.cometrenderer.plugins.bettercompiler.BetterCompilerTags;

public class Programs {
    public final GLProgram POSITION = createProgram(false, false);
    public final GLProgram POSITION_COLOR = createProgram(true, false);
    public final GLProgram POSITION_TEXTURE = createProgram(false, true);
    public final GLProgram POSITION_COLOR_TEXTURE = createProgram(true, true);

    private static GLProgram createProgram(boolean useVertexColor, boolean useTexture) {
        GLShader vertex = GLShader.builder(GLSLLoader.IN_JAR)
                .info(
                        ShaderEntries.DEFAULT_VERTEX,
                        ShaderType.Vertex
                )
                .beforeBuildConsumer(builder -> {
                    BetterCompilerBuilderInfo info = builder.getFromTag(BetterCompilerTags.PROGRAM_INFO);

                    if (useVertexColor)
                        info.define("VERTEX_COLOR");
                    if (useTexture)
                        info.define("VERTEX_UV");
                })
                .build();

        GLShader fragment = GLShader.builder(GLSLLoader.IN_JAR)
                .info(
                        ShaderEntries.DEFAULT_FRAGMENT,
                        ShaderType.Fragment
                )
                .beforeBuildConsumer(builder -> {
                    BetterCompilerBuilderInfo info = builder.getFromTag(BetterCompilerTags.PROGRAM_INFO);

                    if (useVertexColor)
                        info.define("VERTEX_COLOR");
                    if (useTexture) {
                        info.define("VERTEX_UV");
                        builder.sampler("u_Texture");
                    }
                })
                .build();

        return GLProgram.builder(CometRenderer.getColorSnippet())
                .name("")
                .shader(vertex)
                .shader(fragment)
                .build();
    }
}
