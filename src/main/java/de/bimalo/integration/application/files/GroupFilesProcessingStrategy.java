package de.bimalo.integration.application.files;

import de.bimalo.integration.entity.RouteNameConstants;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileEndpoint;
import org.apache.camel.component.file.GenericFileOperations;
import org.apache.camel.component.file.GenericFileProcessStrategy;

@Log
public class GroupFilesProcessingStrategy implements GenericFileProcessStrategy {

  @Override
  public void prepareOnStartup(GenericFileOperations operations, GenericFileEndpoint endpoint)
      throws Exception {}

  @Override
  public boolean begin(
      GenericFileOperations operations,
      GenericFileEndpoint endpoint,
      Exchange exchange,
      GenericFile file)
      throws Exception {
    log.fine(String.format("call begin(file=%s)", file.getAbsoluteFilePath()));

    String groupKey = getFilenameOnly(file);
    String currentDirectory = getCurrentDirectory(file);
    Object[] files = operations.listFiles(currentDirectory);

    // filter files in directory based on the file name of the trigger file
    List<String> groupedFiles =
        Arrays.stream(files)
            .filter(
                f -> {
                  try {
                    String filename = getFilename(f);
                    return filename.startsWith(groupKey)
                        && !filename.equalsIgnoreCase(file.getFileName());
                  } catch (Exception ex) {
                    log.warning(ex.getMessage());
                  }
                  return false;
                })
            .map(
                f -> {
                  try {
                    return getFilename(f);
                  } catch (Exception ex) {
                    log.warning(ex.getMessage());
                  }
                  return "unknown";
                })
            .collect(Collectors.toList());

    exchange.getIn().setHeader(RouteNameConstants.GROUPED_FILES_NAME, groupedFiles);
    exchange.getIn().setHeader(RouteNameConstants.GROUPKEY_NAME, groupKey);

    return true;
  }

  @Override
  public void abort(
      GenericFileOperations operations,
      GenericFileEndpoint endpoint,
      Exchange exchange,
      GenericFile file)
      throws Exception {}

  @Override
  public void commit(
      GenericFileOperations operations,
      GenericFileEndpoint endpoint,
      Exchange exchange,
      GenericFile file)
      throws Exception {}

  @Override
  public void rollback(
      GenericFileOperations operations,
      GenericFileEndpoint endpoint,
      Exchange exchange,
      GenericFile file)
      throws Exception {}

  /**
   * Tries to retrieve the file name using reflection, because the type of object used as file is
   * not clear at this point.
   *
   * @param file the file
   * @return the name of the file
   */
  private String getFilename(Object file)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method getNameMethod = file.getClass().getMethod("getName");
    return (String) getNameMethod.invoke(file);
  }

  private String getCurrentDirectory(GenericFile file) {
    String endpointPath = file.getEndpointPath();
    if (endpointPath.startsWith("/")) {
      return endpointPath;
    } else {
      return String.format("/%s", endpointPath);
    }
  }

  private String getFilenameOnly(GenericFile file) {
    String filename = file.getFileNameOnly();
    if (filename.endsWith(".xml")) {
      return filename.substring(0, filename.length()-4);
    }
    else {
      return filename;
    }
  }
}
