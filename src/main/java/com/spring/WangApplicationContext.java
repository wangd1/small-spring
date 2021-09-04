package com.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangd1
 * @date 2021/9/4
 */
public class WangApplicationContext {

    private Class configClass;
    // bean定义map
    private Map<String,BeanDefinition> beanDefinitionMap = new HashMap<>();
    // 单例池map
    private Map<String,Object> singletonMap = new HashMap<>();
    // BeanPostProcessor list
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public WangApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 扫描
        scan(configClass);
        beanDefinitionMap.forEach((beanName,beanDefinition)->{
            if("singleton".equals(beanDefinition.getScope())){
                singletonMap.put(beanName,createBean(beanName,beanDefinition));
            }
        });
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getType();
        Object instance = null;
        try {
            instance = clazz.getConstructor().newInstance();
            // 依赖注入
            for (Field field : clazz.getDeclaredFields()) {
                if(field.isAnnotationPresent(Autowired.class)){
                    field.setAccessible(true);
                    field.set(instance,getBean(field.getName()));
                }
            }
            // aware
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // BeanPostProcessing Before
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                // 返回了代理对象
                instance = beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            }
            // 初始化操作 InitializingBean
            if(instance instanceof InitializingBean){
                ((InitializingBean) instance).afterPropertiesSet();
            }
            // BeanPostProcessing After
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                // 返回了代理对象
                instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    private void scan(Class configClass) {
        if (this.configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            path = path.replace(".","/");

            ClassLoader classLoader = WangApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            if(resource!=null) {
                File file = new File(resource.getFile());
                // 获取类文件
                if(file.isDirectory()){
                    for (File f : file.listFiles()) {
                        String absolutePath = f.getAbsolutePath();
                        absolutePath = absolutePath.substring(absolutePath.indexOf("com"),absolutePath.indexOf(".class"));
                        absolutePath = absolutePath.replace("/",".");
                        try {
                            Class<?> clazz = classLoader.loadClass(absolutePath);
                            if(clazz.isAnnotationPresent(Component.class)){
                                // 如果是BeanPostProcessor
                                if(BeanPostProcessor.class.isAssignableFrom(clazz)){
                                    BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.getConstructor().newInstance();
                                    beanPostProcessorList.add(beanPostProcessor);
                                    continue;
                                }

                                // 获取到带component的类
                                String beanName = clazz.getAnnotation(Component.class).value();
                                if("".equals(beanName)){
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }
                                // 创建一个BeanDefinition
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);
                                // 单例
                                beanDefinition.setScope("singleton");
                                if(clazz.isAnnotationPresent(Scope.class)){
                                    // 获取scope值,判断单例还是其他
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                }
                                beanDefinitionMap.put(beanName,beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public Object getBean(String beanName) {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new RuntimeException("不包含"+beanName);
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if("singleton".equals(beanDefinition.getScope())){
            // 单例
            Object singletonBean = singletonMap.get(beanName);
            //如果要注入的单例bean还未生成,需要加个判断
            if(singletonBean==null){
                singletonBean = createBean(beanName, beanDefinition);
                singletonMap.put(beanName, singletonBean);
            }
            return singletonBean;
        }else{
            // 原型
            return createBean(beanName,beanDefinition);
        }
    }
}
