
import React, {useEffect, useState} from "react";
import {Upload, Button, List, message, notification} from "antd";
import { UploadOutlined, DownloadOutlined } from "@ant-design/icons";
import { saveAs } from "file-saver";
import "./App.css";

const App = () => {
    const [fileList, setFileList] = useState([]);
    const [processedFiles, setProcessedFiles] = useState([]);
    const pageSessionId = `page_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

    useEffect(() => {
        const ws = new WebSocket('ws://' + window.location.host + '/fileExpiry');
        ws.onmessage = (event) => {
            console.log(event.data)
            const { fileName } = JSON.parse(event.data);
            setProcessedFiles((prev) => prev.filter((name) => name !== fileName));
            notification.info({
                message: '文件过期',
                description: `${fileName} 已过期删除`,
                placement: 'topRight',
            });
        }
        return () => ws.close();
    }, []);

    const handleUpload = async () => {
        if (!fileList.length) {
            message.warning("请先选择文件");
            return;
        }

        const formData = new FormData();
        formData.append("file", fileList[0]);

        try {
            const response = await fetch("/api/getSqlWithExcel", {
                method: "POST",
                headers: { "Session-Id": pageSessionId },
                body: formData,
            });
            const data = await response.json();

            if (data.success) {
                setProcessedFiles((prev) => [...new Set([...prev, ...data.fileNames])]);
                message.success("文件处理成功");
            } else {
                message.error(`处理失败: ${data.message}`);
            }
        } catch (error) {
            console.error("Error:", error);
            message.error("处理过程中发生错误");
        }

        // remove the file from fileList after 10s
        setTimeout(() => setFileList([]), 10000);
    };

    const handleDownload = async (fileName) => {
        try {
            const response = await fetch(`/api/downloadSqlFile?requestName=${fileName}`, {
                headers: { "Session-Id": pageSessionId },
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText);
            }

            const blob = await response.blob();
            saveAs(blob, fileName);
        } catch (error) {
            message.error(`下载过程中发生错误: ${error.message}`);
        }
    };

    return (
        <div className="container">
            <div className="upload-container">
                <div className="upload-header">
                    <h1>Excel转Sql</h1>
                    <p>请选择需要处理的Excel文件</p>
                </div>
                <Upload
                    beforeUpload={(file) => {
                        setFileList([file]);
                        return false;
                    }}
                    fileList={fileList}
                    onRemove={() => setFileList([])}
                    accept=".xls,.xlsx"
                >
                    <Button icon={<UploadOutlined />}>选择文件</Button>
                </Upload>
                <Button
                    type="primary"
                    onClick={handleUpload}
                    style={{ marginTop: "15px" }}
                >
                    开始处理
                </Button>
            </div>

            {processedFiles.length > 0 && (
                <div className="result-container">
                    <h2>处理完成：</h2>
                    <List
                        dataSource={processedFiles}
                        renderItem={(fileName) => (
                            <List.Item
                                actions={[
                                    <Button
                                        type="link"
                                        icon={<DownloadOutlined />}
                                        onClick={() => handleDownload(fileName)}
                                    >
                                        下载
                                    </Button>,
                                ]}
                            >
                                {fileName}
                            </List.Item>
                        )}
                    />
                </div>
            )}
        </div>
    );
};

export default App;