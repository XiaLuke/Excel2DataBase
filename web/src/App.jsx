import React, {useEffect, useState,useRef} from "react";
import {Upload, Button, List, message, notification,Select} from "antd";
import {UploadOutlined, DownloadOutlined} from "@ant-design/icons";
import {saveAs} from "file-saver";
import "./App.css";

const translations = {
    zh: {
        title: "Excel转Sql",
        selectFile: "请选择需要处理的Excel文件",
        chooseFile: "选择文件",
        startProcessing: "开始处理",
        processingCompleted: "处理完成：",
        download: "下载",
        fileProcessed: "文件处理成功",
        fileProcessingError: "处理过程中发生错误",
        fileProcessingFailed: "处理失败",
        downProcessingFailed:"下载过程中发生错误"
    },
    en: {
        title: "Excel to SQL",
        selectFile: "Please select the Excel file to process",
        chooseFile: "Choose File",
        startProcessing: "Start Processing",
        processingCompleted: "Processing Completed:",
        download: "Download",
        fileProcessed: "File processed successfully",
        fileProcessingError: "An error occurred during processing",
        fileProcessingFailed: "Processing failed",
        downProcessingFailed:"An error occurred during the download."
    },
    jp: {
        title: "ExcelからSQLへ",
        selectFile: "処理するExcelファイルを選択してください",
        chooseFile: "ファイルを選択",
        startProcessing: "処理を開始",
        processingCompleted: "処理完了:",
        download: "ダウンロード",
        fileProcessed: "ファイル処理が成功しました",
        fileProcessingError: "処理中にエラーが発生しました",
        fileProcessingFailed: "処理に失敗しました",
        downProcessingFailed: "ダウンロード中にエラーが発生しました。",
    }
};

const App = () => {
    const [fileList, setFileList] = useState([]);
    const [processedFiles, setProcessedFiles] = useState([]);
    const [language, setLanguage] = useState("zh");

    // const pageSessionId = `page_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const pageSessionIdRef = useRef(`page_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`);
    const pageSessionId = pageSessionIdRef.current;

    useEffect(() => {
        const ws = new WebSocket(`ws://localhost:9898/fileExpiry?sessionId=${pageSessionId}`);
        ws.onmessage = (event) => {
            console.log(event.data)
            // const { fileName } = JSON.parse(event.data);
            const fileName = event.data;
            console.log(processedFiles)
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
            message.warning(translations[language].selectFile);
            return;
        }

        const formData = new FormData();
        formData.append("file", fileList[0]);

        try {
            const response = await fetch("/api/getSqlWithExcel", {
                method: "POST",
                headers: {"Session-Id": pageSessionId},
                body: formData,
            });
            const data = await response.json();

            if (data.success) {
                setProcessedFiles((prev) => [...new Set([...prev, data.fileNames])]);
                // message.success("文件处理成功");
                message.success(translations[language].fileProcessed);

            } else {
                // message.error(`处理失败: ${data.message}`);
                message.error(`${translations[language].fileProcessingFailed}: ${data.message}`);
            }
        } catch (error) {
            console.error("Error:", error);
            // message.error("处理过程中发生错误");
            message.error(translations[language].fileProcessingError);
        }

        // remove the file from fileList after 10s
        setTimeout(() => setFileList([]), 10000);
    };

    const handleDownload = async (fileName) => {
        try {
            const response = await fetch(`/api/downloadSqlFile?requestName=${fileName}`, {
                headers: {"Session-Id": pageSessionId},
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText);
            }

            const blob = await response.blob();
            saveAs(blob, fileName);
        } catch (error) {
            // message.error(`下载过程中发生错误: ${error.message}`);
            message.error(`translations[language].downProcessingFailed: ${error.message}`);

        }
    };

    return (
        <>
            <div className="language-select">
                <Select
                    defaultValue="zh"
                    style={{width: 120, float: "right"}}
                    onChange={setLanguage}
                    options={[
                        {value: "zh", label: "中文"},
                        {value: "en", label: "English"},
                        {value: "jp", label: "日本語"}
                    ]}
                />
            </div>
            <div className="container">
                <div className="upload-container">
                    <div className="upload-header">
                        {/*<h1>Excel转Sql</h1>*/}
                        {/*<p>请选择需要处理的Excel文件</p>*/}
                        <h1>{translations[language].title}</h1>
                        <p>{translations[language].selectFile}</p></div>
                    <Upload
                        beforeUpload={(file) => {
                            setFileList([file]);
                            return false;
                        }}
                        fileList={fileList}
                        onRemove={() => setFileList([])}
                        accept=".xls,.xlsx"
                    >
                        {/*<Button icon={<UploadOutlined/>}>选择文件</Button>*/}
                        <Button icon={<UploadOutlined/>}>{translations[language].chooseFile}</Button>

                    </Upload>
                    <Button
                        type="primary"
                        onClick={handleUpload}
                        style={{marginTop: "15px"}}
                    >
                        {/*开始处理*/}
                        {translations[language].startProcessing}

                    </Button>
                </div>

                {processedFiles.length > 0 && (
                    <div className="result-container">
                        {/*<h2>处理完成：</h2>*/}
                        <h2>{translations[language].processingCompleted}</h2>
                        <List
                            dataSource={processedFiles}
                            renderItem={(fileName) => (
                                <List.Item
                                    actions={[
                                        <Button
                                            type="link"
                                            icon={<DownloadOutlined/>}
                                            onClick={() => handleDownload(fileName)}
                                        >
                                            {/*下载*/}
                                            {translations[language].download}
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
        </>

    );
};

export default App;