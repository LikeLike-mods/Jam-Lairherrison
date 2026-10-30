package net.ussm.the_shell.client.water;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.ussm.the_shell.TheShell;
import org.jetbrains.annotations.Nullable;

public final class WaveShaderPatcher {
    public static final String PREFIX = "theshell_";
    private static final String MARKER = PREFIX + "applyWaves";
    private static final Pattern GLOBALS = Pattern.compile("\\b(ShoreMap|MapInfo|WavePhase|WaveAmplitude|shoreData|oceanPhases"
            + "|beachFactor|shorePhase|waveHeight|Impact[0-7]|ImpactRadii[01]|impactPulse|accumulateImpact|impactField"
            + "|PixelOrigin|FoamStrength|AboveWater|grain|foamAmount|applyWaterSurface)\\b");
    private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*#moj_import.*$");
    private static final Pattern MAIN = Pattern.compile("void\\s+main\\s*\\(\\s*(void)?\\s*\\)");
    private static final Pattern VERT_INIT_CALL = Pattern.compile("_vert_init\\s*\\(\\s*\\)\\s*;");
    private static final Pattern REGION_OFFSET = Pattern.compile("uniform\\s+vec3\\s+u_RegionOffset\\s*;");
    private static final Pattern IRIS_NORMAL = Pattern.compile("irs_Normal\\s*=\\s*decodeOct24");
    private static final Pattern IRIS_TANGENT = Pattern.compile("irs_Tangent\\s*=");
    private static final Pattern SODIUM_FOG = Pattern.compile("fragColor\\s*=\\s*_linearFog\\s*\\(");
    private static final Pattern LAST_BRACE = Pattern.compile("}\\s*$");

    private WaveShaderPatcher() {}

    public static String patchSodiumVertex(String source) {
        if (source.contains(MARKER)) return source;
        Matcher main = MAIN.matcher(source);
        String library = library(false);
        if (library == null || !main.find() || !source.contains("vec3 position =")) return unchanged("Sodium vertex shader", source);
        String head = source.substring(0, main.start());
        String body = source.substring(main.start());
        Matcher init = VERT_INIT_CALL.matcher(body);
        Matcher end = LAST_BRACE.matcher(body);
        if (!init.find() || !end.find()) return unchanged("Sodium vertex shader", source);
        body = body.substring(0, end.start())
                + "    " + PREFIX + "surfacePosition = position;\n"
                + "    " + PREFIX + "waterLight = texture(u_LightTex, _vert_tex_light_coord).rgb;\n"
                + body.substring(end.start());
        body = body.substring(0, init.end()) + " " + MARKER + "();" + body.substring(init.end());
        String block = "out vec3 " + PREFIX + "surfacePosition;\n"
                + "out vec3 " + PREFIX + "waterLight;\n"
                + "out float " + PREFIX + "waterSurface;\n"
                + library + vertexFunction("");
        TheShell.LOGGER.info("Ocean waves patched into Sodium's terrain shaders");
        return head + block + body;
    }

    public static String patchSodiumFragment(String source) {
        if (source.contains(PREFIX + "applyWaterSurface")) return source;
        Matcher main = MAIN.matcher(source);
        String library = library(true);
        if (library == null || !main.find()) return unchanged("Sodium fragment shader", source);
        String head = source.substring(0, main.start());
        String body = source.substring(main.start());
        Matcher fog = SODIUM_FOG.matcher(body);
        if (!fog.find()) return unchanged("Sodium fragment shader", source);
        body = body.substring(0, fog.start())
                + "color = " + PREFIX + "applyWaterSurface(color, " + PREFIX + "surfacePosition, " + PREFIX + "waterSurface, "
                + PREFIX + "waterLight, vec4(1.0));\n    " + body.substring(fog.start());
        String block = "in vec3 " + PREFIX + "surfacePosition;\n"
                + "in vec3 " + PREFIX + "waterLight;\n"
                + "in float " + PREFIX + "waterSurface;\n"
                + library;
        return head + block + body;
    }

    public static String patchIrisVertex(String program, @Nullable String source) {
        if (source == null || source.contains(MARKER)) return source;
        Matcher main = MAIN.matcher(source);
        String library = library(false);
        if (library == null || !main.find()) return source;
        String head = source.substring(0, main.start());
        String body = source.substring(main.start());
        Matcher init = VERT_INIT_CALL.matcher(body);
        if (!init.find()) return source;
        body = body.substring(0, init.end()) + MARKER + "();" + body.substring(init.end());
        StringBuilder normal = new StringBuilder();
        if (IRIS_NORMAL.matcher(source).find()) {
            normal.append("    if (abs(irs_Normal.y) > 0.9) {\n")
                    .append("        vec2 dx = vec2(0.5, 0.0);\n")
                    .append("        vec2 dz = vec2(0.0, 0.5);\n")
                    .append("        float hx = ").append(height("p.xz + dx")).append(" - ").append(height("p.xz - dx")).append(";\n")
                    .append("        float hz = ").append(height("p.xz + dz")).append(" - ").append(height("p.xz - dz")).append(";\n")
                    .append("        irs_Normal = normalize(vec3(-hx, 1.0, -hz)) * sign(irs_Normal.y);\n");
            if (IRIS_TANGENT.matcher(source).find()) {
                normal.append("        irs_Tangent.xyz = normalize(irs_Tangent.xyz - irs_Normal * dot(irs_Normal, irs_Tangent.xyz));\n");
            }
            normal.append("    }\n");
        }
        String block = (REGION_OFFSET.matcher(head).find() ? "" : "uniform vec3 u_RegionOffset;\n")
                + "float " + PREFIX + "waterSurface;\n"
                + library + vertexFunction(normal.toString());
        TheShell.LOGGER.info("Ocean waves patched into shaderpack program {}", program);
        return head + block + body;
    }

    private static String height(String at) {
        return PREFIX + "waveHeight(" + at + ", " + PREFIX + "shoreData(" + at + "))";
    }

    private static String vertexFunction(String extra) {
        return "uniform float " + PREFIX + "WavesOn;\n"
                + "uniform float " + PREFIX + "SeaOffset;\n"
                + "uniform vec4 " + PREFIX + "WaterStill;\n"
                + "uniform vec4 " + PREFIX + "WaterFlow;\n"
                + "uniform vec4 " + PREFIX + "WaterOverlay;\n"
                + "bool " + PREFIX + "inSprite(vec2 uv, vec4 bounds) {\n"
                + "    return all(greaterThan(uv, bounds.xy)) && all(lessThan(uv, bounds.zw));\n"
                + "}\n"
                + "void " + MARKER + "() {\n"
                + "    " + PREFIX + "waterSurface = 0.0;\n"
                + "    if (" + PREFIX + "WavesOn < 0.5) return;\n"
                + "    vec2 uv = _vert_tex_diffuse_coord;\n"
                + "    if (!" + PREFIX + "inSprite(uv, " + PREFIX + "WaterStill) && !" + PREFIX + "inSprite(uv, " + PREFIX + "WaterFlow)\n"
                + "            && !" + PREFIX + "inSprite(uv, " + PREFIX + "WaterOverlay)) return;\n"
                + "    vec3 p = _vert_position + _get_draw_translation(_draw_id) + u_RegionOffset;\n"
                + "    if (abs(p.y - " + PREFIX + "SeaOffset) >= 0.30) return;\n"
                + "    " + PREFIX + "waterSurface = 1.0;\n"
                + "    _vert_position.y += " + height("p.xz") + ";\n"
                + extra
                + "}\n";
    }

    @Nullable
    private static String library(boolean withFoam) {
        String impacts = read("impacts.glsl");
        String waves = read("waves.glsl");
        String foam = withFoam ? read("foam.glsl") : "";
        if (impacts == null || waves == null || foam == null) return null;
        String glsl = IMPORT.matcher(impacts + "\n" + waves + "\n" + foam + "\n").replaceAll("");
        return GLOBALS.matcher(glsl).replaceAll(PREFIX + "$1");
    }

    @Nullable
    private static String read(String file) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "shaders/include/" + file);
        try (BufferedReader reader = Minecraft.getInstance().getResourceManager().openAsReader(location)) {
            return String.join("\n", reader.lines().toList());
        } catch (IOException e) {
            TheShell.LOGGER.warn("Ocean waves: cannot read {}", location, e);
            return null;
        }
    }

    private static String unchanged(String what, String source) {
        TheShell.LOGGER.warn("Ocean waves: unexpected {}, it is left untouched (no waves with this Sodium version)", what);
        return source;
    }
}
