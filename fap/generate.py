#!/usr/bin/env python
import os
import subprocess
import sys

def main():
    directorioOriginal = os.getcwd()
    modelPath = os.path.join(directorioOriginal, "app", "led", "fap")
    targetPath =  os.path.join(directorioOriginal)
    params = "solicitud=false"
        
    generatorDir = os.path.join(directorioOriginal, 'compiler')
    generatorLibDir = os.path.join(generatorDir, 'lib')
    jars = []
    
    #Desarrollo
    jars.append(os.path.join(directorioOriginal, "compiler/lib/*")) 
    jars.append(os.path.join(directorioOriginal, "compiler/src/es.fap.simpleled.generator/bin/"))
    jars.append(os.path.join(directorioOriginal, "compiler/src/es.fap.simpleled/bin/"))
    
    #Variables para ejecutar el script    
    separador = ':'
    if os.name == 'nt':
        separador = ';'

    if os.path.exists(os.path.join(targetPath, "app/DiffGen.patch")):
            os.remove(os.path.join(targetPath, "app/DiffGen.patch"))
    diffParam = "false"
    for elements in sys.argv:
        if (elements == "--diff"):
            diffParam = "true"
    
    classpath = separador.join(str(x) for x in jars)

    log = "file:///" + os.path.join(generatorDir, "config", "log4.properties")
        
    os.chdir(generatorDir);
    class_name = "org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher"
    
    fapModelPath = os.path.join(directorioOriginal, "app", "led");
    workflow = "workflow.LedGenerator";
    
    # app.java_path()
    cmd = ["java", "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9009", "-Dfile.encoding=utf-8","-classpath", classpath, class_name, workflow, "-p", "targetPath=" + targetPath+"/", "modelPath=" + modelPath, "fapModelPath=" + fapModelPath, "diffParam=" + diffParam, params];
    print (' '.join(cmd));
    subprocess.call(cmd);

if __name__ == "__main__":
    main()