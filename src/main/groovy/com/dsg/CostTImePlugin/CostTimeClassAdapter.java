package com.dsg.CostTImePlugin;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ASM8;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;

import com.android.ddmlib.Log;
import com.squareup.haha.perflib.ClassObj;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import jdk.nashorn.internal.codegen.CompilerConstants;

/**
 * @author DSG
 * @Project ASMCostTime
 * @date 2020/6/22
 * @describe
 * ClassVisitor 的调用必须是遵循下面的调用顺序的：
 *
 *  结合这个看 https://www.jianshu.com/p/16ed4d233fd1
 *  visit visitSource? visitOuterClass? ( visitAnnotation | visitAttribute )*
 * ( visitInnerClass | visitField | visitMethod )*
 * visitEnd

 */
public class CostTimeClassAdapter extends ClassVisitor {
    public boolean changed; //是否修改过
    private String owner;
    private boolean isInterface;
    private String className;

    public CostTimeClassAdapter(ClassVisitor visitor,String className) {
        super(ASM8, visitor);
        MethodCall;
        ExprEditor;
        CompilerConstants.FieldAccess;
        this.className = className;
    }

    public CostTimeClassAdapter(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        owner = name;
        isInterface = (access & ACC_INTERFACE) != 0;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        m

        Log.e("mmb", "visitMethod->" + name+"------>"+className);
        if (!isInterface && mv != null && !name.equals("<init>")) {
            //将MethodVisitor交由CostTimeMethodAdapter代理
            mv = new CostTimeMethodAdapter(access, name, descriptor, mv);
        }
        return mv;

    }

    //继承自LocalVariablesSorter 有序遍历素有方法
    class CostTimeMethodAdapter extends LocalVariablesSorter {
        private String name;
        private boolean isAnnotationed;
        private int time;

        public CostTimeMethodAdapter(int access, String name, String descriptor, MethodVisitor methodVisitor) {
            super(ASM8, access, descriptor, methodVisitor);
            this.name = name;
        }


        /**
         * 遍历代码的开始 声明一个局部变量
         */
        @Override
        public void visitCode() {
            super.visitCode();

//            isAnnotationed = true;
//            Log.e("mmb", "isAnnotationed----33333-->" + isAnnotationed);
//            if (!changed && isAnnotationed) {
//                changed = true;
//            }
            if (isAnnotationed) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                time = newLocal(Type.LONG_TYPE);
                mv.visitVarInsn(LSTORE, time);
            }
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            super.visitIntInsn(opcode, operand);
        }


        /**
         * 遍历操作码 判断是否是return语句 如果是return 就插入我们的代码
         *
         * @param opcode 操作码
         */
        @Override
        public void visitInsn(int opcode) {
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                if (isAnnotationed) {
                    //这里的代码都可以由ASM插件生成
                    //Label可以生成局部变量
                    Label l1 = new Label();
                    mv.visitLabel(l1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                    mv.visitVarInsn(LLOAD, time);
                    mv.visitInsn(LSUB);
                    mv.visitVarInsn(LSTORE, 3);
                    Label l2 = new Label();
                    mv.visitLabel(l2);
                    mv.visitLdcInsn(owner);
                    mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                    mv.visitLdcInsn("mmb func " + name + " cost Time:");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                    mv.visitVarInsn(LLOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
                    mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false);
                }
            }
            super.visitInsn(opcode);
        }


        /**
         * @param descriptor 最先执行 判断是否存在注解 如果存在 就进行插桩
         * //读取方法上的注解
         */
        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            isAnnotationed = ("Lcom/dsg/annotations/MothodCostTime;".equals(descriptor));
//            isAnnotationed = true;
            Log.e("mmb", "isAnnotationed------>" + isAnnotationed);
            if (!changed && isAnnotationed) {
                changed = true;
            }
            Log.e("mmb", "changed------>" + changed);
            return super.visitAnnotation(descriptor, visible);

        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack, maxLocals);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
        }
    }
}
