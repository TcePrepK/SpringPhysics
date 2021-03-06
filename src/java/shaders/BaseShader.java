package shaders;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import toolbox.Color;
import toolbox.Vector3D;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL45.*;

public abstract class BaseShader {
    private final int programID;

    private final int vertexShaderID;
    private final int fragmentShaderID;
    private final int computeShaderID;

    BaseShader(final String vertexFile, final String fragmentFile) {
        vertexShaderID = BaseShader.loadShader(vertexFile, GL_VERTEX_SHADER);
        fragmentShaderID = BaseShader.loadShader(fragmentFile, GL_FRAGMENT_SHADER);
        computeShaderID = 0;

        programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        bindAttributes();
        glLinkProgram(programID);
        glValidateProgram(programID);
        getAllUniformLocations();
    }

    public BaseShader(final String computeFile) {
        vertexShaderID = 0;
        fragmentShaderID = 0;
        computeShaderID = BaseShader.loadShader(computeFile, GL_COMPUTE_SHADER);

        programID = glCreateProgram();
        glCreateShader(computeShaderID);
        glAttachShader(programID, computeShaderID);
        bindAttributes();
        glLinkProgram(programID);
        glValidateProgram(programID);
        getAllUniformLocations();
    }

    protected abstract void getAllUniformLocations();

    int getUniformLocation(final String uniformName) {
        return glGetUniformLocation(programID, uniformName);
    }

    public void start() {
        glUseProgram(programID);
    }

    public static void stop() {
        glUseProgram(0);
    }

    public void cleanUp() {
        BaseShader.stop();
        glDetachShader(programID, vertexShaderID);
        glDetachShader(programID, fragmentShaderID);
        glDetachShader(programID, computeShaderID);
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
        glDeleteShader(computeShaderID);
        glDeleteProgram(programID);
    }

    protected abstract void bindAttributes();

    void bindAttribute(final int attribute, final String variableName) {
        glBindAttribLocation(programID, attribute, variableName);
    }

    protected static void loadFloat(final int location, final float value) {
        glUniform1f(location, value);
    }

    protected static void loadInt(final int location, final int value) {
        glUniform1i(location, value);
    }

    static void load2DVector(final int location, final Vector2f vector) {
        glUniform2f(location, vector.x(), vector.y());
    }

    protected static void load3DVector(final int location, final Vector3D vector) {
        glUniform3f(location, vector.x, vector.y, vector.z);
    }

    protected static void load4DVector(final int location, final Vector4f vector) {
        glUniform4f(location, vector.x(), vector.y(), vector.z(), vector.w());
    }

    protected static void load4DVector(final int location, final Color vector) {
        glUniform4f(location, vector.getR(), vector.getG(), vector.getB(), vector.getA());
    }

    protected static void loadBoolean(final int location, final boolean value) {
        float toLoad = 0;
        if (value) {
            toLoad = 1;
        }
        glUniform1f(location, toLoad);
    }

    protected static void loadMatrix(final int location, final Matrix4f matrix) {
        final float[] buffer = new float[16];
        matrix.get(buffer);
        glUniformMatrix4fv(location, false, buffer);
    }

    private static void shaderReader(final StringBuilder shaderSource, final String file, final List<String> includeList) {
        try {
            final BufferedReader reader = new BufferedReader(new FileReader("res" + file));
            String line;
            while ((line = reader.readLine()) != null) {
                final Pattern commentPattern = Pattern.compile("\\/\\/[^\\n\\r]+");
                final Matcher commentMatcher = commentPattern.matcher(line);
                if (commentMatcher.find()) {
                    continue;
                }

                final Pattern includePattern = Pattern.compile("(?<=#include )[\\/\\w+.]+");
                final Matcher includeMatcher = includePattern.matcher(line);
                if (includeMatcher.find()) {
                    final String includePath = includeMatcher.group(0);

                    if (includeList.contains(includePath)) {
                        throw new IOException("Circular Dependency!");
                    }

                    includeList.add(includePath);
                    BaseShader.shaderReader(shaderSource, includePath, includeList);
                } else {
                    shaderSource.append(line).append("\n");
                }
            }
            reader.close();
        } catch (final IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static int loadShader(final String file, final int type) {
        final StringBuilder shaderSource = new StringBuilder();
        BaseShader.shaderReader(shaderSource, file, new ArrayList<>());

        final int shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println(glGetShaderInfoLog(shaderID, 1000));
            System.err.println("Could not compile shader!");
            System.exit(-1);
        }
        return shaderID;
    }
}
