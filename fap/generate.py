#!/usr/bin/env python
import os
import subprocess

def main():
    directorioOriginal = os.getcwd()
    modelPath = os.path.join(directorioOriginal, "app", "led", "fap")
    targetPath =  os.path.join(directorioOriginal)
    params = "solicitud=false"
    
    #print "Directorio: ",directorioOriginal
    #print "Model: ",modelPath
    #print "Target: ",targetPath
    #execute_workflow(modelPath, targetPath, params, args, app)
    
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
    
    classpath = separador.join(str(x) for x in jars)

    log = "file:///" + os.path.join(generatorDir, "config", "log4.properties")
        
    os.chdir(generatorDir);
    class_name = "org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher"
    
    fapModelPath = os.path.join(directorioOriginal, "app", "led");
    workflow = "workflow.LedGenerator";
    
    # app.java_path()
    cmd = ["java", "-Dfile.encoding=utf-8","-classpath", classpath, class_name, workflow, "-p", "targetPath=" + targetPath+"/", "modelPath=" + modelPath, "fapModelPath=" + fapModelPath, params];
    #print cmd
    subprocess.call(cmd);


if __name__ == "__main__":
    main()