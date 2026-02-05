package com.silverio.tasks.shared.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
  Instant timestamp,
  int status,
  String erro,
  String mensagem,
  String caminho,
  Map<String, String> campos
) {}
