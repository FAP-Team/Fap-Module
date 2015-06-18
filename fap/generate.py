#!/usr/bin/env python
import os
import subprocess
import sys, getopt

class CommandError(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        repr(self.value)

def debugCommand(argv):
    if ("--debug" in argv) or ("-d" in argv):
        # Verifica que se asigno el puerto
        length = len(argv)
        try:
            posDebug = argv.index("--debug") 
            if posDebug == length - 1:
                print '--debug <port> or -d <port>'
                raise CommandError("Argumento incorrecto")
        except ValueError:
            try:
                posDebug = argv.index("-d") 
                if posDebug == length - 1:
                    print '--debug <port> or -d <port>'
                    raise CommandError("Argumento incorrecto")
            except ValueError:
                # Este caso no se puede dar
                pass
         
        try:
            # Verifica que el puerto es un valor entero
            port = long(argv[posDebug + 1])
        except ValueError:
            print '--debug <port> or -d <port>'
            raise CommandError("Argumento incorrecto")
        
        return "-agentlib:jdwp=server=y,transport=dt_socket,address=" + str(port) + ",suspend=y"
    
    return None
    
def main(argv):
    
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
    #cmd = ["java", "-Dlog4j.configuration=" + log, "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=f,address=9009", "-Dfile.encoding=utf-8","-classpath", classpath, class_name, workflow, "-p", "targetPath=" + targetPath+"/", "modelPath=" + modelPath, "fapModelPath=" + fapModelPath, "diffParam=" + diffParam, params];
    #cmd = ["java", "-Dlog4j.configuration=" + log, "-Dfile.encoding=utf-8","-classpath", classpath, class_name, workflow, "-p", "targetPath=" + targetPath+"/", "modelPath=" + modelPath, "fapModelPath=" + fapModelPath, "diffParam=" + diffParam, params];
    cmd = ["java", "-Dlog4j.configuration=" + log, "-Dfile.encoding=utf-8","-classpath", classpath, class_name, workflow, "-p", "targetPath=" + targetPath+"/", "modelPath=" + modelPath, "fapModelPath=" + fapModelPath, "diffParam=" + diffParam, params];
    
    try:
        debug = debugCommand(argv)
        if (debug != None):
            cmd.insert(2, debug)
    except CommandError:
        sys.exit(2)
        
    print (' '.join(cmd));
    subprocess.call(cmd);

if __name__ == "__main__":
    main(sys.argv[1:])