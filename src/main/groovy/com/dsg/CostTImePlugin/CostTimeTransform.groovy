package com.dsg.CostTImePlugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import groovyjarjarasm.asm.tree.FieldNode
import groovyjarjarasm.asm.tree.MethodNode
import jdk.internal.org.objectweb.asm.tree.ClassNode
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

class CostTimeTransform extends Transform {

    //Transform名称
    @Override
    String getName() {
        return "CostTime"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        //遍历输入
        for (TransformInput input in inputs) {
            //遍历Directioy
            for (DirectoryInput dirInput in input.directoryInputs) {
                //处理需要插桩的文件
                modifyClassWithPath(dirInput.file)
                //Copy修改之后的文件
                File dest = outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes,
                        dirInput.scopes, Format.DIRECTORY)
                //替换文件
                FileUtils.copyDirectory(dirInput.file, dest)
            }

            //遍历JarInput 因为我们这里只对自己的方法插桩 所以不对JarInput做处理
            for (JarInput jarInput : input.jarInputs) {//jar（第三方库，module）
                if (jarInput.scopes.contains(QualifiedContent.Scope.SUB_PROJECTS)) {//module library
                    //从module中获取注解信息
//                    readClassWithJar(jarInput)
                }
                //虽然不做处理 但是还是要记得重新拷贝回去 不然会有问题
                copyFile(jarInput, outputProvider)
            }
        }
    }

    void modifyClassWithPath(File dir) {
        def root = dir.absolutePath
        dir.eachFileRecurse { File file ->
            def filePath = file.absolutePath
            //过滤非class文件
            if (!filePath.endsWith(".class")) return
            def className = getClassName(root, filePath)
            //过滤系统文件
            if (isSystemClass(className)) return
            //hook关键代码
            hookClass(filePath, className)
        }
    }

    //AMS写入
    void hookClass(String filePath, String className) {
        println className + " 1111  一个.class文件相对于一个类:--333-->"+className
        //1.声明ClassReader
        ClassReader reader = new ClassReader(new FileInputStream(new File(filePath)))

        ClassNode classNode = new ClassNode(Opcodes.ASM5);
        classReader.accept(classNode, 0);
        getClass().getde

        List<MethodNode> methods = classNode.methods;
        methods.get(0).cle
        List<FieldNode> fields = classNode.fields;

        println className + " 2  hookClass55555555555555:"
        //2声明 ClassWriter
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
        println className + " 3  hookClass55555555555555:"
        //3声明ClassVisitor
        CostTimeClassAdapter adapter = new CostTimeClassAdapter(writer,className)
        println className + " 4  hookClass55555555555555:"
        //4调用accept方法 传入classVisitor
        reader.accept(adapter, ClassReader.EXPAND_FRAMES)
        println className + " 5  hookClass55555555555555:--->"+adapter.changed
        if (adapter.changed) {
            println className + "7  mmb is changed:" + adapter.changed
            byte[] bytes = writer.toByteArray()
            FileOutputStream fos = new FileOutputStream(new File(filePath))
            fos.write(bytes)
        }

        println className + " 7  hookClass55555555555555:"
    }


    //默认排除
    static final DEFAULT_EXCLUDE = [
            '^android\\..*',
            '^androidx\\..*',
            '.*\\.R$',
            '.*\\.R\\$.*$',
            '.*\\.BuildConfig$',
    ]

    //获取类名
    String getClassName(String root, String classPath) {
        return classPath.substring(root.length() + 1, classPath.length() - 6)
                .replaceAll("/", ".")       // unix/linux
                .replaceAll("\\\\", ".")    //windows
    }

    boolean isSystemClass(String fileName) {
        for (def exclude : DEFAULT_EXCLUDE) {
            if (fileName.matches(exclude)) return true
        }
        return false
    }
    void copyFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        def dest = getDestFile(jarInput, outputProvider)
        ExprEditor
        FileUtils.copyFile(jarInput.file, dest)
    }

    static File getDestFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        def destName = jarInput.name
        // 重名名输出文件,因为可能同名,会覆盖
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        // 获得输出文件
        def dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        return dest
    }

}