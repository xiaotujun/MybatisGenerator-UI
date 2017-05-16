package me.lishuo.service;

import me.lishuo.util.FileUtil;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.lishuo.util.FileUtil.deleteDir;

/**
 * Created by lis on 17/5/15.
 */
@Service
public class GenService {


    public String genCode(HttpServletRequest request) {

        String ip = request.getParameter("ip");
        String db = request.getParameter("db");
        String port = request.getParameter("port");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String modelPackageName = request.getParameter("modelpackagename");
        String daoPackageName = request.getParameter("daopackagename");
        String mapperPath = request.getParameter("mapperpath");
        String tableNames[] = request.getParameterValues("tablenames");
        String tableModels[] = request.getParameterValues("tablemodels");
        String path = "mbg";
        // 清空临时文件夹下所有内容
        deleteDir(new File(System.getProperty("user.dir") + "/mbg"));

        try {
            List<String> warnings = new ArrayList<>();
            boolean overwrite = true;

            File dirFile = new File(path);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }

            String config_path = "src/main/resources/Configuration.xml";
            File configFile = new File(config_path);
            ConfigurationParser parser = new ConfigurationParser(warnings);
            Configuration config = parser.parseConfiguration(configFile);
            Context context = config.getContexts().get(0);

            // db
            JDBCConnectionConfiguration jdbcConnectionConfiguration = context.getJdbcConnectionConfiguration();
            String connection = "jdbc:mysql://" + ip + ":" + port + "/" + db;
            jdbcConnectionConfiguration.setConnectionURL(connection);
            jdbcConnectionConfiguration.setUserId(userName);
            jdbcConnectionConfiguration.setPassword(password);
            // model 配置
            JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = context.getJavaModelGeneratorConfiguration();
            javaModelGeneratorConfiguration.setTargetPackage(modelPackageName);
            javaModelGeneratorConfiguration.setTargetProject(path);
            // DAO 配置
            JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = context.getJavaClientGeneratorConfiguration();
            javaClientGeneratorConfiguration.setTargetPackage(daoPackageName);
            javaClientGeneratorConfiguration.setTargetProject(path);
            // Mapper
            SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = context.getSqlMapGeneratorConfiguration();
            sqlMapGeneratorConfiguration.setTargetPackage(mapperPath);
            sqlMapGeneratorConfiguration.setTargetProject(path);
            // 表
            List<TableConfiguration> tableConfigurations = context.getTableConfigurations();
            tableConfigurations.clear();
            for (int i = 0; i < tableNames.length; i++) {
                if (!tableNames[i].isEmpty() && !tableModels[i].isEmpty()) {
                    TableConfiguration tableConfiguration = new TableConfiguration(context);
                    tableConfiguration.setTableName(tableNames[i]);
                    tableConfiguration.setDomainObjectName(tableModels[i]);
                    tableConfiguration.setCountByExampleStatementEnabled(false);
                    tableConfiguration.setDeleteByExampleStatementEnabled(false);
                    tableConfiguration.setSelectByExampleStatementEnabled(false);
                    tableConfiguration.setUpdateByExampleStatementEnabled(false);
                    tableConfiguration.getProperties().setProperty("useActualColumnNames", "true");
                    tableConfigurations.add(tableConfiguration);
                }
            }

            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(null);

            FileUtil.compress("tmp", System.getProperty("user.dir") + "/result/mbg.zip");
        } catch (Exception e) {
            e.printStackTrace();
            return "01";
        }

        return "00";
    }

}

