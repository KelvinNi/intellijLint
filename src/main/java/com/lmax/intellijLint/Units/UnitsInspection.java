package com.lmax.intellijLint.Units;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.SpecialAnnotationsUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Storage;
import com.intellij.psi.*;
import com.siyeh.ig.ui.ExternalizableStringSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

@SuppressWarnings("WeakerAccess") //Needs to be public as is used in plugin.
@Storage("com.lmax.intellijLint.units.xml")
public class UnitsInspection extends BaseJavaLocalInspectionTool implements PersistentStateComponent<UnitsInspection.State> {
    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Mismatched units";
    }

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "LMAX";
    }

    public static final String DESCRIPTION_TEMPLATE = "Assigning %s to variable of type %s";
    public static final String BINARY_EXPRESSION_DESCRIPTION_TEMPLATE = "Left side of expression is %s and right side is %s";
    public static final String RETURNING_DESCRIPTION_TEMPLATE = "Returning %s when expecting %s";

    @SuppressWarnings("PublicField")
    public final List<String> subTypeAnnotations = new ArrayList<>();

    private PsiMethod walkUpToWrappingMethod(PsiElement element)
    {
        if (element == null)
        {
            return null;
        }

        PsiElement parent = element.getParent();
        if (parent == null)
        {
            return null;
        }

        if (parent instanceof PsiMethod) {
            return (PsiMethod) parent;
        } else {
            return walkUpToWrappingMethod(parent);
        }
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitAssignmentExpression(PsiAssignmentExpression expression) {
                super.visitAssignmentExpression(expression);

                SubType declared = SubType.getSubType(expression.getLExpression());
                SubType assigned = SubType.getSubType(expression.getRExpression());
                inspect(assigned, declared, holder);
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                final SubType initializer = SubType.getSubType(field.getInitializer());
                final SubType declared = SubType.getSubType(field);
                inspect(initializer, declared, holder);
            }

            @Override
            public void visitLocalVariable(PsiLocalVariable variable) {
                super.visitLocalVariable(variable);

                final SubType initializer = SubType.getSubType(variable.getInitializer());

                final SubType declared = SubType.getSubType(variable);
                inspect(initializer, declared, holder);
            }

            @Override
            public void visitReturnStatement(PsiReturnStatement statement) {
                super.visitReturnStatement(statement);

                final SubType returnValue = SubType.getSubType(statement.getReturnValue());

                PsiMethod psiMethod = walkUpToWrappingMethod(statement.getReturnValue());
                final SubType declared = SubType.getSubType(psiMethod);

                inspect(returnValue, declared, holder, RETURNING_DESCRIPTION_TEMPLATE);
            }

            @Override
            public void visitBinaryExpression(PsiBinaryExpression expression) {
                super.visitBinaryExpression(expression);

                PsiExpression rOperand = expression.getROperand();
                if (rOperand == null)
                {
                    return;
                }

                inspect(expression, SubType.getSubType(expression.getLOperand()), SubType.getSubType(rOperand), holder, BINARY_EXPRESSION_DESCRIPTION_TEMPLATE);
            }

            @Override
            public void visitConditionalExpression(PsiConditionalExpression expression) {
                super.visitConditionalExpression(expression);

                PsiExpression elseExpression = expression.getElseExpression();
                if (elseExpression == null)
                {
                    return;
                }

                inspect(expression, SubType.getSubType(expression.getThenExpression()), SubType.getSubType(elseExpression), holder, BINARY_EXPRESSION_DESCRIPTION_TEMPLATE);
            }
        };
    }

    private void inspect(SubType potentiallyProblematic, SubType checkAgainst, @NotNull ProblemsHolder holder)
    {
        inspect(potentiallyProblematic, checkAgainst, holder, DESCRIPTION_TEMPLATE);
    }

    private void inspect(SubType potentiallyProblematic, SubType checkAgainst, @NotNull ProblemsHolder holder, String descriptionTemplate)
    {
        if (!Objects.equals(potentiallyProblematic, checkAgainst)) {
            String description = String.format(descriptionTemplate, potentiallyProblematic.getSubtypeFQN(), checkAgainst.getSubtypeFQN());
            holder.registerProblem(potentiallyProblematic.getPsiElement(), description);
        }
    }

    private void inspect(PsiElement element, SubType left, SubType right, @NotNull ProblemsHolder holder, String descriptionTemplate)
    {
        if (!Objects.equals(left, right)) {
            String description = String.format(descriptionTemplate, left.getSubtypeFQN(), right.getSubtypeFQN());
            holder.registerProblem(element, description);
        }
    }


    public JComponent createOptionsPanel() {
        return SpecialAnnotationsUtil.createSpecialAnnotationsListControl(
                SubType.subTypeAnnotations, "Sub Type annotations");
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    @Nullable
    @Override
    public UnitsInspection.State getState() {
        State state = new State();
        state.subTypeAnnotations = new HashSet<>(this.subTypeAnnotations);
        return state;
    }

    @Override
    public void loadState(UnitsInspection.State state) {
        SubType.setAnnotations(state.subTypeAnnotations);
    }

    public class State {
        public State()
        {
            subTypeAnnotations = new ExternalizableStringSet("org.checkerframework.framework.qual.SubtypeOf");
        }

        public Set<String> subTypeAnnotations;
    }
}