package net.ussm.examplemod.client.mask;

import com.mojang.blaze3d.vertex.PoseStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class MaskTargetSelectionTest {
    private static int checks;

    public static void main(String[] args) {
        GeoBone root = bone(null, "root");
        GeoBone panel = bone(root, "panel");
        GeoBone mesh = bone(panel, "mesh");
        GeoBone detail = bone(mesh, "detail");
        GeoBone ornament = bone(root, "ornament");
        GeoBone handle = bone(root, "handle");
        GeoBone prefixed = bone(root, "panel_extra");
        GeoBone differentCase = bone(root, "Panel");
        GeoBone blade = bone(root, "blade");
        GeoBone mask = bone(panel, "mask");
        GeoBone control = bone(mask, "control");
        GeoBone nestedTarget = bone(control, "ornament");
        GeoBone secondaryMask = bone(panel, "mask_secondary");

        Set<String> targets = Set.of("panel", "ornament");
        check(MaskState.isInClippedTree(panel, targets), "first configured bone");
        check(MaskState.isInClippedTree(ornament, targets), "second configured bone");
        check(MaskState.isInClippedTree(mesh, targets), "child with an unrelated name");
        check(MaskState.isInClippedTree(detail, targets), "deep descendant");
        check(!MaskState.isInClippedTree(root, targets), "parent of a target stays intact");
        check(!MaskState.isInClippedTree(handle, targets), "unselected sibling stays intact");
        check(!MaskState.isInClippedTree(prefixed, targets), "names are exact, without implicit prefixes");
        check(!MaskState.isInClippedTree(differentCase, targets), "names are case-sensitive");
        check(!MaskState.isInClippedTree(blade, targets), "blade has no special treatment");
        check(MaskState.isInClippedTree(blade, Set.of("blade")), "lightsaber configuration");
        check(!MaskState.isInClippedTree(panel, Set.of("handle")), "independent renderer configuration");
        check(MaskState.isInClippedTree(handle, Set.of("handle")), "independent renderer target");
        check(!MaskState.isInClippedTree(mask, targets), "control beneath a selected parent");
        check(!MaskState.isInClippedTree(control, targets), "control descendant");
        check(!MaskState.isInClippedTree(nestedTarget, targets), "selected name inside a control tree");
        check(!MaskState.isInClippedTree(secondaryMask, targets), "mask prefix remains reserved");
        check(!MaskState.isInClippedTree(mask, Set.of("mask")), "controls always take priority");

        BakedGeoModel model = new BakedGeoModel(List.of(root), null);
        List<String> warnings = new ArrayList<>();
        MaskState.capture(new PoseStack(), model, targets, warnings::add);
        check(warnings.isEmpty(), "custom targets are recognized during volume capture");

        warnings.clear();
        MaskState.capture(new PoseStack(), model, Set.of("missing_part"), warnings::add);
        check(warnings.size() == 1 && warnings.getFirst().contains("missing_part"),
                "missing target diagnostic includes the configured name");

        warnings.clear();
        MaskState.capture(new PoseStack(), model, Set.of("control"), warnings::add);
        check(warnings.size() == 1 && warnings.getFirst().contains("control"),
                "a control descendant cannot satisfy the target configuration");

        System.out.println("Mask target selection: " + checks + " checks passed.");
    }

    private static GeoBone bone(GeoBone parent, String name) {
        GeoBone bone = new GeoBone(parent, name, false, null, false, false);
        if (parent != null) parent.getChildBones().add(bone);
        return bone;
    }

    private static void check(boolean passed, String description) {
        checks++;
        if (!passed) throw new AssertionError(description);
    }
}
