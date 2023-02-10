package ir.darkdeveloper.bitkip.repo;

import ir.darkdeveloper.bitkip.models.DownloadModel;
import ir.darkdeveloper.bitkip.models.QueueModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ir.darkdeveloper.bitkip.repo.DatabaseHelper.*;

public class DownloadsRepo {
    private static final DatabaseHelper dbHelper = new DatabaseHelper();

    public static void createTable() {
        dbHelper.createDownloadsTable();
    }

    public static void insertDownload(DownloadModel download) {
        var downloadSql = "INSERT INTO " + DOWNLOADS_TABLE_NAME + " (" +
                COL_NAME + "," +
                COL_PROGRESS + "," +
                COL_SIZE + "," +
                COL_URL + "," +
                COL_PATH + "," +
                COL_CHUNKS + "," +
                COL_ADD_DATE + "," +
                COL_LAST_TRY_DATE + "," +
                COL_COMPLETE_DATE + ")" +
                " VALUES(\"" +
                download.getName() + "\"," +
                download.getProgress() + "," +
                download.getSize() + ",\"" +
                download.getUrl() + "\",\"" +
                download.getFilePath() + "\"," +
                download.getChunks() + ",\"" +
                download.getAddDate().toString() + "\",\"" +
                download.getLastTryDate().toString() + "\",\"" +
                download.getCompleteDate().toString() + "\"" +
                ");";
        try (var con = dbHelper.openConnection();
             var stmt = con.createStatement()) {
            dbHelper.insertDownload(downloadSql, download, stmt);
            download.getQueue().forEach(queue -> {
                var queueDownloadSql = """
                        INSERT INTO queue_download (download_id, queue_id)
                        VALUES (%d, %d);
                        """.formatted(download.getId(), queue.getId());
                try {
                    stmt.executeUpdate(queueDownloadSql);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DownloadModel findById(int id) {
        var sql = "SELECT * FROM " + DOWNLOADS_TABLE_NAME + " WHERE " + COL_ID + "=" + id + ";";
        try (var con = dbHelper.openConnection();
             var stmt = con.createStatement();
             var rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return createDownload(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<DownloadModel> getDownloads() {
        var sql = """
                SELECT *, q.name as queue_name
                FROM downloads
                         INNER JOIN queue_download ON
                    downloads.id = queue_download.download_id
                         INNER JOIN queues q on q.id = queue_download.queue_id;
                                """;
        return fetchDownloads(sql);
    }

    public static List<DownloadModel> getDownloadsByQueue(Long id) {
        var sql = """
                SELECT *, q.name as queue_name
                FROM downloads d
                         INNER JOIN queue_download qd ON d.id = qd.download_id
                         INNER JOIN queues q ON q.id = qd.queue_id
                WHERE qd.queue_id = %d;
                """.formatted(id);
        return fetchDownloads(sql);
    }

    private static List<DownloadModel> fetchDownloads(String sql) {
        var list = new ArrayList<DownloadModel>();
        try (var con = dbHelper.openConnection();
             var stmt = con.createStatement();
             var rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(createDownload(rs));
            return list.stream().distinct().toList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void updateDownloadQueue(int download_id, int queue_id) {
        var queueCountSql = """
                SELECT count(*) AS queue_count
                FROM downloads
                         INNER JOIN queue_download qd ON downloads.id = qd.download_id;
                """;
        var insertQueueDownloadSql = """
                INSERT INTO queue_download (download_id, queue_id) VALUES (%d,%d);
                """.formatted(download_id, queue_id);
        var idOfNotDefaultQueueSql = """
                SELECT queue_id
                FROM queue_download qd
                         INNER JOIN queues q ON q.id = qd.queue_id
                WHERE q.name != 'All Downloads'
                  AND q.name != 'Compressed'
                  AND q.name != 'Programs'
                  AND q.name != 'Videos'
                  AND q.name != 'Music'
                  AND q.name != 'Docs'
                  AND q.name != 'Others'
                  AND qd.download_id = %d;
                """.formatted(download_id);
        var updateQueueSql = """
                UPDATE queue_download
                SET queue_id = %d
                WHERE queue_id = %d;
                """;

        try (var con = dbHelper.openConnection();
             var stmt = con.createStatement()) {
            var countRS = stmt.executeQuery(queueCountSql);
            countRS.next();
            var queueCount = countRS.getInt("queue_count");
            if (queueCount == 3) {
                var prevQueueRS = stmt.executeQuery(idOfNotDefaultQueueSql);
                prevQueueRS.next();
                var prevQueueId = prevQueueRS.getInt("queue_id");
                stmt.executeUpdate(updateQueueSql.formatted(prevQueueId, queue_id));
            } else if (queueCount == 2)
                stmt.executeUpdate(insertQueueDownloadSql);
            else throw new Exception("queue count for the download is not correct");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteDownload(DownloadModel download) {
        var sql = "DELETE FROM " + DOWNLOADS_TABLE_NAME + " WHERE " + COL_ID + "=" + download.getId() + ";";
        try (var con = dbHelper.openConnection();
             var stmt = con.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static DownloadModel createDownload(ResultSet rs) throws SQLException {
        var id = rs.getInt(COL_ID);
        var name = rs.getString(COL_NAME);
        var progress = rs.getInt(COL_PROGRESS);
        var size = rs.getInt(COL_SIZE);
        var url = rs.getString(COL_URL);
        var filePath = rs.getString(COL_PATH);
        var chunks = rs.getInt(COL_CHUNKS);
        var queueId = rs.getInt("queue_id");
        var queueName = rs.getString("queue_name");
        var queueEditable = rs.getBoolean(COL_EDITABLE);
        var queueCanAddDown = rs.getBoolean(COL_CAN_ADD_DOWN);
        var queue = new QueueModel(queueId, queueName, queueEditable, queueCanAddDown);
        var addDate = rs.getString(COL_ADD_DATE);
        var lastTryDate = rs.getString(COL_LAST_TRY_DATE);
        var completeDate = rs.getString(COL_COMPLETE_DATE);
        var dow = DownloadModel.builder()
                .id(id).name(name).progress(progress).size(size).url(url).filePath(filePath)
                .chunks(chunks).queue(new ArrayList<>(List.of(queue))).remainingTime(0).addDate(LocalDateTime.parse(addDate))
                .lastTryDate(LocalDateTime.parse(lastTryDate)).completeDate(LocalDateTime.parse(completeDate))
                .build();
        dow.fillProperties();
        return dow;
    }

    public static void updateDownloadSize(DownloadModel download) {
        var sql = "UPDATE " + DOWNLOADS_TABLE_NAME + " SET " + COL_SIZE + "=\"" + download.getSize() + "\""
                + " WHERE " + COL_ID + "=" + download.getId() + ";";
        try (var con = dbHelper.openConnection();
             var stmt = con.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
