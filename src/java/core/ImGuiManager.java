package core;

import display.DisplayManager;
import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import toolbox.Logger;
import toolbox.Octatree;

import java.util.List;

public class ImGuiManager {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public ImGuiManager() {
        ImGui.createContext();
        imGuiGlfw.init(DisplayManager.getWindow(), true);
        imGuiGl3.init("#version 450");

        Logger.out("~ ImGui Initialized Successfully");
    }

    public static void renderBranch(final Octatree branch, final String name, final int id) {
        if (ImGui.treeNode("##" + id)) {
            ImGui.sameLine();
            ImGui.selectable(name, true);
            if (branch.isBranched()) {
                final List<Octatree> branches = branch.getBranches();
                for (int i = 0; i < branches.size(); i++) {
                    ImGuiManager.renderBranch(branches.get(i), "Branch " + (i + 1), i);
                }
            } else {
                ImGui.text(" ~PointAmount: " + branch.getPointAmount());
            }
            ImGui.treePop();
        } else {
            ImGui.sameLine();
            ImGui.selectable(name);
        }
    }

    public void update(final double renderTime) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
        ImGui.begin("Cool Window");

        // FPS
        ImGui.text("FPS: " + DisplayManager.getFPS());
        ImGui.text("Rendering time: " + renderTime + "ms");
        ImGui.spacing();
        ImGui.spacing();
        // FPS

        ImGui.end();
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void cleanUp() {
        imGuiGlfw.dispose();
        imGuiGl3.dispose();
        ImGui.destroyContext();
    }
}